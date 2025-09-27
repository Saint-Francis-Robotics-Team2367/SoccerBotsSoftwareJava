package com.soccerbots.control.controller;

import com.soccerbots.control.robot.RobotManager;
import net.java.games.input.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ControllerManager {
    private static final Logger logger = LoggerFactory.getLogger(ControllerManager.class);
    
    private final RobotManager robotManager;
    private final Map<String, GameController> connectedControllers;
    private final Map<String, String> controllerRobotPairings;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    
    private boolean isPolling = false;
    private volatile boolean emergencyStopActive = false;
    
    public ControllerManager(RobotManager robotManager) {
        this.robotManager = robotManager;
        this.connectedControllers = new ConcurrentHashMap<>();
        this.controllerRobotPairings = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.scheduledExecutor = Executors.newScheduledThreadPool(2);
        
        startControllerDetection();
        startInputPolling();
    }
    
    private void startControllerDetection() {
        scheduledExecutor.scheduleWithFixedDelay(this::detectControllers, 0, 5, TimeUnit.SECONDS);
    }
    
    private void detectControllers() {
        try {
            Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
            
            for (Controller controller : controllers) {
                if (controller.getType() == Controller.Type.GAMEPAD || 
                    controller.getType() == Controller.Type.STICK) {
                    
                    String controllerId = generateControllerId(controller);
                    
                    if (!connectedControllers.containsKey(controllerId)) {
                        GameController gameController = new GameController(controllerId, controller);
                        connectedControllers.put(controllerId, gameController);
                        logger.info("Detected new controller: {}", controller.getName());
                    }
                }
            }
            
            removeDisconnectedControllers();
            
        } catch (Exception e) {
            logger.error("Error detecting controllers", e);
        }
    }
    
    private void removeDisconnectedControllers() {
        connectedControllers.entrySet().removeIf(entry -> {
            try {
                Controller controller = entry.getValue().getController();
                if (!controller.poll()) {
                    logger.info("Controller disconnected: {}", controller.getName());
                    controllerRobotPairings.remove(entry.getKey());
                    return true;
                }
            } catch (Exception e) {
                logger.warn("Error polling controller, removing: {}", entry.getKey());
                controllerRobotPairings.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    private String generateControllerId(Controller controller) {
        return controller.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_" + 
               Math.abs(controller.hashCode());
    }
    
    private void startInputPolling() {
        if (isPolling) {
            return;
        }
        
        isPolling = true;
        scheduledExecutor.scheduleAtFixedRate(this::pollControllerInputs, 0, 16, TimeUnit.MILLISECONDS);
    }
    
    private void pollControllerInputs() {
        for (GameController gameController : connectedControllers.values()) {
            try {
                Controller controller = gameController.getController();
                if (!controller.poll()) {
                    continue;
                }
                
                ControllerInput input = readControllerInput(controller);
                gameController.updateInput(input);
                
                String pairedRobotId = controllerRobotPairings.get(gameController.getId());
                if (pairedRobotId != null && !emergencyStopActive && input.hasMovement()) {
                    robotManager.sendMovementCommand(
                        pairedRobotId,
                        input.getForward(),
                        input.getSideways(),
                        input.getRotation()
                    );
                } else if (pairedRobotId != null && !emergencyStopActive && input.isStopCommand()) {
                    robotManager.sendStopCommand(pairedRobotId);
                }
                
            } catch (Exception e) {
                logger.error("Error polling controller input", e);
            }
        }
    }
    
    private ControllerInput readControllerInput(Controller controller) {
        ControllerInput input = new ControllerInput();
        
        Component[] components = controller.getComponents();
        for (Component component : components) {
            float value = component.getPollData();
            String name = component.getName();
            
            switch (name.toLowerCase()) {
                case "x":
                case "x axis":
                    input.setLeftStickX(value);
                    break;
                case "y":
                case "y axis":
                    input.setLeftStickY(-value); // Invert Y axis
                    break;
                case "rx":
                case "z rotation":
                    input.setRightStickX(value);
                    break;
                case "ry":
                case "z axis":
                    input.setRightStickY(-value); // Invert Y axis
                    break;
                case "z":
                case "left trigger":
                    input.setLeftTrigger(value);
                    break;
                case "rz":
                case "right trigger":
                    input.setRightTrigger(value);
                    break;
                case "pov":
                case "hat switch":
                    input.setDPad(value);
                    break;
                default:
                    if (component.getIdentifier() instanceof Component.Identifier.Button) {
                        Component.Identifier.Button button = (Component.Identifier.Button) component.getIdentifier();
                        int buttonIndex = button.toString().hashCode() % 16; // Simple mapping for up to 16 buttons
                        input.setButton(buttonIndex, value > 0.5f);
                    }
                    break;
            }
        }
        
        return input;
    }
    
    public void pairControllerWithRobot(String controllerId, String robotId) {
        if (connectedControllers.containsKey(controllerId)) {
            controllerRobotPairings.put(controllerId, robotId);
            
            if (robotManager.getRobot(robotId) != null) {
                robotManager.getRobot(robotId).setPairedControllerId(controllerId);
            }
            
            logger.info("Paired controller {} with robot {}", controllerId, robotId);
        } else {
            logger.warn("Cannot pair - controller not found: {}", controllerId);
        }
    }
    
    public void unpairController(String controllerId) {
        String robotId = controllerRobotPairings.remove(controllerId);
        if (robotId != null) {
            if (robotManager.getRobot(robotId) != null) {
                robotManager.getRobot(robotId).setPairedControllerId(null);
            }
            logger.info("Unpaired controller {} from robot {}", controllerId, robotId);
        }
    }
    
    public List<GameController> getConnectedControllers() {
        return new ArrayList<>(connectedControllers.values());
    }
    
    public int getConnectedControllerCount() {
        return connectedControllers.size();
    }
    
    public GameController getController(String controllerId) {
        return connectedControllers.get(controllerId);
    }
    
    public String getPairedRobotId(String controllerId) {
        return controllerRobotPairings.get(controllerId);
    }
    
    public Map<String, String> getAllPairings() {
        return new ConcurrentHashMap<>(controllerRobotPairings);
    }
    
    public void stopPolling() {
        isPolling = false;
    }
    
    public void startPolling() {
        if (!isPolling) {
            startInputPolling();
        }
    }

    public void activateEmergencyStop() {
        emergencyStopActive = true;
        logger.warn("Emergency stop activated - controller inputs disabled");
        robotManager.emergencyStopAll();
    }

    public void deactivateEmergencyStop() {
        emergencyStopActive = false;
        logger.info("Emergency stop deactivated - controller inputs re-enabled");
    }

    public boolean isEmergencyStopActive() {
        return emergencyStopActive;
    }
    
    public void shutdown() {
        logger.info("Shutting down controller manager");
        isPolling = false;
        
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
            }
        }
        
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}
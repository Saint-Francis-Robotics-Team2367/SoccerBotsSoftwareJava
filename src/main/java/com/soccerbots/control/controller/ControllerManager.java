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
    private final Map<String, Boolean> controllerEnabled; // Track enabled/disabled state
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;

    private boolean isPolling = false;
    private volatile boolean emergencyStopActive = false;
    
    public ControllerManager(RobotManager robotManager) {
        this.robotManager = robotManager;
        this.connectedControllers = new ConcurrentHashMap<>();
        this.controllerRobotPairings = new ConcurrentHashMap<>();
        this.controllerEnabled = new ConcurrentHashMap<>();
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
            // Force environment refresh to detect newly connected controllers
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
            if (env == null) {
                logger.warn("Controller environment is not available");
                return;
            }

            Controller[] controllers;
            try {
                controllers = env.getControllers();
            } catch (UnsatisfiedLinkError e) {
                logger.warn("JInput native libraries not available. Controller support disabled.");
                logger.warn("Error: {}", e.getMessage());
                logger.warn("Java library path: {}", System.getProperty("java.library.path"));
                return;
            } catch (Exception e) {
                logger.error("Failed to get controllers from environment", e);
                return;
            }

            if (controllers == null) {
                logger.info("No controllers array returned from environment (NULL)");
                return;
            }

            logger.info("Scanning {} total input devices from JInput", controllers.length);

            for (Controller controller : controllers) {
                if (controller == null) {
                    continue;
                }

                logger.debug("Found controller: {} (Type: {})", controller.getName(), controller.getType());

                // Check for game controllers, joysticks, and generic gamepads
                // Includes PS4, PS5 DualSense, Xbox controllers
                if (controller.getType() == Controller.Type.GAMEPAD ||
                    controller.getType() == Controller.Type.STICK ||
                    controller.getType() == Controller.Type.FINGERSTICK ||
                    controller.getType() == Controller.Type.TRACKBALL ||
                    (controller.getName() != null && (
                        controller.getName().toLowerCase().contains("gamepad") ||
                        controller.getName().toLowerCase().contains("controller") ||
                        controller.getName().toLowerCase().contains("xbox") ||
                        controller.getName().toLowerCase().contains("playstation") ||
                        controller.getName().toLowerCase().contains("dualsense") ||
                        controller.getName().toLowerCase().contains("dualshock") ||
                        controller.getName().toLowerCase().contains("ps4") ||
                        controller.getName().toLowerCase().contains("ps5") ||
                        controller.getName().toLowerCase().contains("ps")))) {

                    // Test if we can poll the controller
                    try {
                        if (!controller.poll()) {
                            logger.debug("Controller {} failed initial poll test", controller.getName());
                            continue;
                        }
                    } catch (Exception pollException) {
                        logger.debug("Controller {} polling exception: {}", controller.getName(), pollException.getMessage());
                        continue;
                    }

                    String controllerId = generateControllerId(controller);

                    if (!connectedControllers.containsKey(controllerId)) {
                        GameController gameController = new GameController(controllerId, controller);
                        connectedControllers.put(controllerId, gameController);
                        controllerEnabled.putIfAbsent(controllerId, true); // Enable by default
                        logger.info("Detected new controller: {} (Type: {})", controller.getName(), controller.getType());
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

                // Check if controller is enabled
                Boolean enabled = controllerEnabled.get(gameController.getId());
                if (enabled == null || !enabled) {
                    continue; // Skip disabled controllers
                }

                ControllerInput input = readControllerInput(controller);
                gameController.updateInput(input);

                String pairedRobotId = controllerRobotPairings.get(gameController.getId());
                if (pairedRobotId != null && !emergencyStopActive && input.hasMovement()) {
                    robotManager.sendMovementCommand(
                        pairedRobotId,
                        input.getLeftStickX(),
                        input.getLeftStickY(),
                        input.getRightStickX(),
                        input.getRightStickY()
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

    public void enableController(String controllerId) {
        if (connectedControllers.containsKey(controllerId)) {
            controllerEnabled.put(controllerId, true);
            logger.info("Enabled controller: {}", controllerId);
        }
    }

    public void disableController(String controllerId) {
        if (connectedControllers.containsKey(controllerId)) {
            controllerEnabled.put(controllerId, false);
            logger.info("Disabled controller: {}", controllerId);

            // Stop robot if it was paired
            String robotId = controllerRobotPairings.get(controllerId);
            if (robotId != null) {
                robotManager.sendStopCommand(robotId);
            }
        }
    }

    public boolean isControllerEnabled(String controllerId) {
        Boolean enabled = controllerEnabled.get(controllerId);
        return enabled == null || enabled; // Default to enabled
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
        robotManager.deactivateEmergencyStop();
        logger.info("Emergency stop deactivated - controller inputs re-enabled");
    }

    public boolean isEmergencyStopActive() {
        return emergencyStopActive;
    }

    public void refreshControllers() {
        logger.info("========== MANUAL CONTROLLER REFRESH REQUESTED ==========");
        try {
            // Force a complete environment refresh for newly connected devices
            logger.info("Refreshing controller environment...");

            // Try to reinitialize the controller environment if possible
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();

            if (env == null) {
                logger.error("Controller environment is NULL - JInput may not be initialized properly");
                return;
            }

            logger.info("Controller environment class: {}", env.getClass().getName());

            // Clear existing controllers first
            logger.info("Current controller count before refresh: {}", connectedControllers.size());

            // Perform detection
            detectControllers();

            logger.info("========== REFRESH COMPLETED: Found {} controllers ==========", connectedControllers.size());

            // Log each detected controller
            for (GameController gc : connectedControllers.values()) {
                logger.info("  - {} (Type: {}, ID: {})", gc.getName(), gc.getType(), gc.getId());
            }

        } catch (Exception e) {
            logger.error("Error during manual controller refresh", e);
            e.printStackTrace();
        }
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
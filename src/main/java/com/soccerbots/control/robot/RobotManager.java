package com.soccerbots.control.robot;

import com.soccerbots.control.network.NetworkManager;
import com.soccerbots.control.controller.ControllerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class RobotManager {
    private static final Logger logger = LoggerFactory.getLogger(RobotManager.class);

    private final NetworkManager networkManager;
    private final Map<String, Robot> connectedRobots;
    private final ExecutorService executorService;

    // Game state management
    private volatile String currentGameState = "standby";

    public RobotManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.connectedRobots = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();

        logger.info("ESP32 Robot Manager initialized");
    }

    /**
     * Add an ESP32 robot manually by IP address and name
     */
    public Robot addRobot(String robotName, String ipAddress) {
        Robot robot = new Robot(robotName, robotName, ipAddress, "connected");
        connectedRobots.put(robotName, robot);
        logger.info("Added ESP32 robot: {} at {}", robotName, ipAddress);
        return robot;
    }

    /**
     * Send controller input to a specific ESP32 robot
     */
    public void sendControllerInput(String robotName, ControllerInput input) {
        Robot robot = connectedRobots.get(robotName);
        if (robot == null) {
            logger.warn("Robot not found: {}", robotName);
            return;
        }

        // Convert controller input to ESP32 format
        ESP32Command command = ESP32Command.fromControllerInput(
            robotName,
            input.getLeftStickX(), input.getLeftStickY(),
            input.getRightStickX(), input.getRightStickY(),
            input.getButton(0), input.getButton(1),
            input.getButton(2), input.getButton(3)
        );

        sendESP32Command(robot, command);
    }

    /**
     * Send ESP32 command to robot
     */
    public void sendESP32Command(Robot robot, ESP32Command command) {
        if (!"teleop".equals(currentGameState)) {
            // Only send movement commands during teleop mode
            command = ESP32Command.createStopCommand(command.getRobotName());
        }

        networkManager.sendRobotCommand(
            command.getRobotName(),
            robot.getIpAddress(),
            command.getLeftX(),
            command.getLeftY(),
            command.getRightX(),
            command.getRightY(),
            command.isCross(),
            command.isCircle(),
            command.isSquare(),
            command.isTriangle()
        );

        robot.updateLastCommandTime();
    }

    /**
     * Send movement command to robot using normalized values
     */
    public void sendMovementCommand(String robotName, double leftStickX, double leftStickY,
                                  double rightStickX, double rightStickY) {
        Robot robot = connectedRobots.get(robotName);
        if (robot == null) {
            logger.warn("Robot not found: {}", robotName);
            return;
        }

        ESP32Command command = ESP32Command.fromControllerInput(
            robotName, leftStickX, leftStickY, rightStickX, rightStickY,
            false, false, false, false // No buttons
        );

        sendESP32Command(robot, command);
    }

    /**
     * Send stop command to specific robot
     */
    public void sendStopCommand(String robotName) {
        Robot robot = connectedRobots.get(robotName);
        if (robot == null) {
            logger.warn("Robot not found: {}", robotName);
            return;
        }

        ESP32Command stopCommand = ESP32Command.createStopCommand(robotName);
        sendESP32Command(robot, stopCommand);
    }

    /**
     * Emergency stop all robots
     */
    public void emergencyStopAll() {
        logger.warn("EMERGENCY STOP - Halting all ESP32 robots");
        setGameState("standby"); // This will stop all movement
        for (Robot robot : connectedRobots.values()) {
            sendStopCommand(robot.getId());
        }
    }

    /**
     * Set game state for all robots
     */
    public void setGameState(String gameState) {
        this.currentGameState = gameState;
        logger.info("Setting game state to: {}", gameState);

        // Broadcast game state to all robots
        for (Robot robot : connectedRobots.values()) {
            networkManager.sendGameStatus(robot.getName(), robot.getIpAddress(), gameState);
        }
    }

    /**
     * Get current game state
     */
    public String getCurrentGameState() {
        return currentGameState;
    }

    /**
     * Start teleop mode (allow robot movement)
     */
    public void startTeleop() {
        setGameState("teleop");
    }

    /**
     * Stop teleop mode (disable robot movement)
     */
    public void stopTeleop() {
        setGameState("standby");
    }

    /**
     * Check if robots can move (teleop mode)
     */
    public boolean isInTeleopMode() {
        return "teleop".equals(currentGameState);
    }

    public List<Robot> getConnectedRobots() {
        return new ArrayList<>(connectedRobots.values());
    }

    public int getConnectedRobotCount() {
        return connectedRobots.size();
    }

    public Robot getRobot(String robotName) {
        return connectedRobots.get(robotName);
    }

    public void removeRobot(String robotName) {
        Robot removed = connectedRobots.remove(robotName);
        if (removed != null) {
            logger.info("Removed ESP32 robot: {}", removed.getName());
        }
    }

    /**
     * Test connection to a robot by sending a ping
     */
    public void testRobotConnection(String robotName) {
        Robot robot = connectedRobots.get(robotName);
        if (robot != null) {
            networkManager.sendGameStatus(robotName, robot.getIpAddress(), "ping");
            logger.info("Testing connection to robot: {}", robotName);
        }
    }

    /**
     * Clear robots that haven't responded recently
     */
    public void clearOfflineRobots() {
        long currentTime = System.currentTimeMillis();
        long timeout = 60000; // 60 seconds for manual robots

        connectedRobots.entrySet().removeIf(entry -> {
            Robot robot = entry.getValue();
            boolean isOffline = (currentTime - robot.getLastSeenTime()) > timeout;
            if (isOffline) {
                logger.info("Removing offline ESP32 robot: {}", robot.getName());
            }
            return isOffline;
        });
    }

    /**
     * Get network status for ESP32 communication
     */
    public boolean isNetworkReady() {
        return networkManager.isNetworkActive();
    }

    /**
     * Check if connected to expected ESP32 network
     */
    public boolean isConnectedToRobotNetwork() {
        return networkManager.isConnectedToExpectedNetwork();
    }

    /**
     * Get expected network name for ESP32 robots
     */
    public String getExpectedNetworkName() {
        return networkManager.getExpectedNetwork();
    }

    public void shutdown() {
        logger.info("Shutting down ESP32 robot manager");

        // Stop all robots before shutdown
        setGameState("standby");

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}
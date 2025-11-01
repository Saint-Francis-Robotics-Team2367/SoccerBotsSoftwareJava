package com.soccerbots.control;

import com.soccerbots.control.api.ApiServer;
import com.soccerbots.control.controller.ControllerManager;
import com.soccerbots.control.network.NetworkManager;
import com.soccerbots.control.robot.RobotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Headless launcher for the robot control system with HTTP API
 * Used when running with Electron frontend
 */
public class HeadlessLauncher {
    private static final Logger logger = LoggerFactory.getLogger(HeadlessLauncher.class);

    public static void main(String[] args) {
        logger.info("Starting SoccerBots Control System (Headless Mode)");

        try {
            // Parse port from args if provided
            int apiPort = 8080;
            if (args.length > 0) {
                try {
                    apiPort = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid port argument, using default: {}", apiPort);
                }
            }

            // Initialize network manager
            NetworkManager networkManager = new NetworkManager();

            // Initialize robot manager
            RobotManager robotManager = new RobotManager(networkManager);
            robotManager.startDiscovery();

            // Initialize controller manager
            ControllerManager controllerManager = new ControllerManager(robotManager);

            // Start API server
            ApiServer apiServer = new ApiServer(robotManager, controllerManager, networkManager);
            apiServer.start(apiPort);

            logger.info("System initialized successfully");
            logger.info("API server running on http://localhost:{}", apiPort);
            logger.info("WebSocket endpoint: ws://localhost:{}/ws", apiPort);

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down...");
                apiServer.stop();
                controllerManager.shutdown();
                robotManager.shutdown();
                networkManager.shutdown();
                logger.info("Shutdown complete");
            }));

            // Keep the application running
            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("Failed to start headless launcher", e);
            System.exit(1);
        }
    }
}

package com.soccerbots.control.api;

import com.soccerbots.control.controller.ControllerManager;
import com.soccerbots.control.controller.GameController;
import com.soccerbots.control.network.NetworkManager;
import com.soccerbots.control.robot.Robot;
import com.soccerbots.control.robot.RobotManager;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.websocket.WsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class ApiServer {
    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);
    private static final int DEFAULT_PORT = 8080;

    private final Javalin app;
    private final RobotManager robotManager;
    private final ControllerManager controllerManager;
    private final NetworkManager networkManager;
    private final Set<org.eclipse.jetty.websocket.api.Session> wsSessions;

    public ApiServer(RobotManager robotManager, ControllerManager controllerManager, NetworkManager networkManager) {
        this.robotManager = robotManager;
        this.controllerManager = controllerManager;
        this.networkManager = networkManager;
        this.wsSessions = new CopyOnWriteArraySet<>();

        this.app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost();
                });
            });
        });

        setupRoutes();
    }

    private void setupRoutes() {
        // Health check
        app.get("/api/health", ctx -> {
            ctx.json(Map.of(
                "status", "online",
                "timestamp", System.currentTimeMillis()
            ));
        });

        // Get all robots
        app.get("/api/robots", this::getRobots);

        // Get robot by ID
        app.get("/api/robots/{id}", this::getRobotById);

        // Connect to robot
        app.post("/api/robots/{id}/connect", this::connectRobot);

        // Disconnect from robot
        app.post("/api/robots/{id}/disconnect", this::disconnectRobot);

        // Enable/disable robot
        app.post("/api/robots/{id}/enable", this::enableRobot);
        app.post("/api/robots/{id}/disable", this::disableRobot);

        // Refresh robot list
        app.post("/api/robots/refresh", this::refreshRobots);

        // Get all controllers
        app.get("/api/controllers", this::getControllers);

        // Emergency stop
        app.post("/api/emergency-stop", this::emergencyStop);
        app.post("/api/emergency-stop/deactivate", this::deactivateEmergencyStop);

        // Network statistics
        app.get("/api/network/stats", this::getNetworkStats);

        // WebSocket for real-time updates
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                wsSessions.add(ctx.session);
                logger.info("WebSocket client connected: {}", ctx.session.getRemoteAddress());
            });

            ws.onClose(ctx -> {
                wsSessions.remove(ctx.session);
                logger.info("WebSocket client disconnected");
            });

            ws.onError(ctx -> {
                logger.error("WebSocket error", ctx.error());
            });
        });

        logger.info("API routes configured");
    }

    private void getRobots(Context ctx) {
        List<Map<String, Object>> robotsList = new ArrayList<>();

        for (Robot robot : robotManager.getDiscoveredRobots()) {
            robotsList.add(robotToMap(robot));
        }

        ctx.json(robotsList);
    }

    private void getRobotById(Context ctx) {
        String id = ctx.pathParam("id");
        Robot robot = robotManager.getRobot(id);

        if (robot != null) {
            ctx.json(robotToMap(robot));
        } else {
            ctx.status(404).json(Map.of("error", "Robot not found"));
        }
    }

    private void connectRobot(Context ctx) {
        String id = ctx.pathParam("id");
        Robot robot = robotManager.getRobot(id);

        if (robot != null) {
            // Robot is already discovered, mark as connected
            ctx.json(Map.of(
                "success", true,
                "message", "Robot connected",
                "robot", robotToMap(robot)
            ));
            broadcastUpdate("robot_connected", robotToMap(robot));
        } else {
            ctx.status(404).json(Map.of("error", "Robot not found"));
        }
    }

    private void disconnectRobot(Context ctx) {
        String id = ctx.pathParam("id");
        Robot robot = robotManager.getRobot(id);

        if (robot != null) {
            // Send stop command
            robotManager.sendStopCommand(id);
            ctx.json(Map.of(
                "success", true,
                "message", "Robot disconnected"
            ));
            broadcastUpdate("robot_disconnected", Map.of("id", id));
        } else {
            ctx.status(404).json(Map.of("error", "Robot not found"));
        }
    }

    private void enableRobot(Context ctx) {
        String id = ctx.pathParam("id");
        ctx.json(Map.of("success", true, "message", "Robot enabled"));
        broadcastUpdate("robot_enabled", Map.of("id", id));
    }

    private void disableRobot(Context ctx) {
        String id = ctx.pathParam("id");
        Robot robot = robotManager.getRobot(id);

        if (robot != null) {
            robotManager.sendStopCommand(id);
        }

        ctx.json(Map.of("success", true, "message", "Robot disabled"));
        broadcastUpdate("robot_disabled", Map.of("id", id));
    }

    private void refreshRobots(Context ctx) {
        robotManager.scanForRobots();
        ctx.json(Map.of(
            "success", true,
            "message", "Scanning for robots"
        ));
        broadcastUpdate("robots_refreshing", Map.of("timestamp", System.currentTimeMillis()));
    }

    private void getControllers(Context ctx) {
        List<Map<String, Object>> controllersList = new ArrayList<>();

        for (GameController controller : controllerManager.getConnectedControllers()) {
            Map<String, Object> controllerData = new HashMap<>();
            controllerData.put("id", controller.getId());
            controllerData.put("name", controller.getName());
            controllerData.put("connected", controller.isConnected());
            controllerData.put("pairedRobotId", controllerManager.getPairedRobotId(controller.getId()));
            controllersList.add(controllerData);
        }

        ctx.json(controllersList);
    }

    private void emergencyStop(Context ctx) {
        controllerManager.activateEmergencyStop();
        ctx.json(Map.of(
            "success", true,
            "message", "Emergency stop activated"
        ));
        broadcastUpdate("emergency_stop", Map.of("active", true));
    }

    private void deactivateEmergencyStop(Context ctx) {
        controllerManager.deactivateEmergencyStop();
        ctx.json(Map.of(
            "success", true,
            "message", "Emergency stop deactivated"
        ));
        broadcastUpdate("emergency_stop", Map.of("active", false));
    }

    private void getNetworkStats(Context ctx) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("latency", Math.random() * 10 + 10); // Mock data for now
        stats.put("bandwidth", Math.random() * 10 + 45); // Mock data for now
        stats.put("activeConnections", robotManager.getDiscoveredRobots().size());

        ctx.json(stats);
    }

    private Map<String, Object> robotToMap(Robot robot) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", robot.getId());
        map.put("name", robot.getName());
        map.put("ipAddress", robot.getIpAddress());
        map.put("status", robot.isConnected() ? "connected" : "disconnected");
        map.put("signal", 85); // Mock signal strength for now
        map.put("disabled", false); // Can be extended later
        map.put("pairedControllerId", robot.getPairedControllerId());
        return map;
    }

    public void broadcastUpdate(String eventType, Object data) {
        Map<String, Object> message = Map.of(
            "type", eventType,
            "data", data,
            "timestamp", System.currentTimeMillis()
        );

        String json;
        try {
            json = com.fasterxml.jackson.databind.ObjectMapper.class
                .getDeclaredConstructor()
                .newInstance()
                .writeValueAsString(message);
        } catch (Exception e) {
            logger.error("Failed to serialize WebSocket message", e);
            return;
        }

        for (var session : wsSessions) {
            try {
                if (session.isOpen()) {
                    session.getRemote().sendString(json);
                }
            } catch (Exception e) {
                logger.error("Failed to send WebSocket message", e);
            }
        }
    }

    public void start() {
        start(DEFAULT_PORT);
    }

    public void start(int port) {
        app.start(port);
        logger.info("API server started on port {}", port);
    }

    public void stop() {
        app.stop();
        logger.info("API server stopped");
    }
}

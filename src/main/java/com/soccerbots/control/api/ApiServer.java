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
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ApiServer {
    private static final Logger logger = LoggerFactory.getLogger(ApiServer.class);
    private static final int DEFAULT_PORT = 8080;

    private final Javalin app;
    private final RobotManager robotManager;
    private final ControllerManager controllerManager;
    private final NetworkManager networkManager;
    private final Set<org.eclipse.jetty.websocket.api.Session> wsSessions;

    // Match timer state
    private long matchDurationMs = 120000; // Default: 2 minutes
    private long matchStartTime = 0;
    private boolean matchRunning = false;
    private final ScheduledExecutorService timerExecutor;
    private int lastControllerCount = 0;

    public ApiServer(RobotManager robotManager, ControllerManager controllerManager, NetworkManager networkManager) {
        this.robotManager = robotManager;
        this.controllerManager = controllerManager;
        this.networkManager = networkManager;
        this.wsSessions = new CopyOnWriteArraySet<>();
        this.timerExecutor = Executors.newScheduledThreadPool(1);

        this.app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost();
                });
            });
        });

        setupRoutes();
        startTimerBroadcast();
        startControllerMonitoring();
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

        // Controller pairing
        app.post("/api/controllers/{controllerId}/pair/{robotId}", this::pairController);
        app.post("/api/controllers/{controllerId}/unpair", this::unpairController);
        app.post("/api/controllers/{controllerId}/enable", this::enableController);
        app.post("/api/controllers/{controllerId}/disable", this::disableController);
        app.post("/api/controllers/refresh", this::refreshControllers);

        // Emergency stop
        app.post("/api/emergency-stop", this::emergencyStop);
        app.post("/api/emergency-stop/deactivate", this::deactivateEmergencyStop);

        // Network statistics
        app.get("/api/network/stats", this::getNetworkStats);

        // Match timer endpoints
        app.get("/api/match/timer", this::getMatchTimer);
        app.post("/api/match/start", this::startMatch);
        app.post("/api/match/stop", this::stopMatch);
        app.post("/api/match/reset", this::resetMatch);
        app.post("/api/match/duration", this::setMatchDuration);

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
        Set<String> addedRobots = new HashSet<>();

        // Add all connected robots first
        for (Robot robot : robotManager.getConnectedRobots()) {
            robotsList.add(robotToMap(robot));
            addedRobots.add(robot.getId());
        }

        // Add discovered robots that aren't already in the list
        for (Robot robot : robotManager.getDiscoveredRobots()) {
            if (!addedRobots.contains(robot.getId())) {
                robotsList.add(robotToMap(robot));
            }
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
            // Connect the robot (move from discovered to connected)
            robotManager.connectDiscoveredRobot(id);
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
            // Send stop command and remove from connected robots
            robotManager.sendStopCommand(id);
            robotManager.removeRobot(id);
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
            controllerData.put("enabled", controllerManager.isControllerEnabled(controller.getId()));
            controllerData.put("type", controller.getType());
            controllersList.add(controllerData);
        }

        ctx.json(controllersList);
    }

    private void pairController(Context ctx) {
        String controllerId = ctx.pathParam("controllerId");
        String robotId = ctx.pathParam("robotId");

        controllerManager.pairControllerWithRobot(controllerId, robotId);
        ctx.json(Map.of(
            "success", true,
            "message", "Controller paired with robot"
        ));
        broadcastUpdate("controller_paired", Map.of(
            "controllerId", controllerId,
            "robotId", robotId
        ));
    }

    private void unpairController(Context ctx) {
        String controllerId = ctx.pathParam("controllerId");

        controllerManager.unpairController(controllerId);
        ctx.json(Map.of(
            "success", true,
            "message", "Controller unpaired"
        ));
        broadcastUpdate("controller_unpaired", Map.of("controllerId", controllerId));
    }

    private void enableController(Context ctx) {
        String controllerId = ctx.pathParam("controllerId");

        controllerManager.enableController(controllerId);
        ctx.json(Map.of(
            "success", true,
            "message", "Controller enabled"
        ));
        broadcastUpdate("controller_enabled", Map.of("controllerId", controllerId));
    }

    private void disableController(Context ctx) {
        String controllerId = ctx.pathParam("controllerId");

        controllerManager.disableController(controllerId);
        ctx.json(Map.of(
            "success", true,
            "message", "Controller disabled"
        ));
        broadcastUpdate("controller_disabled", Map.of("controllerId", controllerId));
    }

    private void refreshControllers(Context ctx) {
        controllerManager.refreshControllers();
        ctx.json(Map.of(
            "success", true,
            "message", "Scanning for controllers"
        ));
        broadcastUpdate("controllers_refreshing", Map.of("timestamp", System.currentTimeMillis()));
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

    private void getMatchTimer(Context ctx) {
        long now = System.currentTimeMillis();
        long timeRemainingMs = matchRunning
            ? Math.max(0, matchDurationMs - (now - matchStartTime))
            : matchDurationMs;

        Map<String, Object> timerData = new HashMap<>();
        timerData.put("running", matchRunning);
        timerData.put("timeRemainingMs", timeRemainingMs);
        timerData.put("durationMs", matchDurationMs);
        timerData.put("timeRemainingSeconds", timeRemainingMs / 1000);

        ctx.json(timerData);
    }

    private void startMatch(Context ctx) {
        if (!matchRunning) {
            matchStartTime = System.currentTimeMillis();
            matchRunning = true;
            robotManager.startTeleop();  // Enable robot movement
            logger.info("Match started - {} seconds", matchDurationMs / 1000);
            broadcastUpdate("match_start", Map.of(
                "durationMs", matchDurationMs,
                "timestamp", matchStartTime
            ));
        }
        ctx.json(Map.of("success", true, "message", "Match started"));
    }

    private void stopMatch(Context ctx) {
        if (matchRunning) {
            matchRunning = false;
            robotManager.stopTeleop();  // Disable robot movement
            logger.info("Match stopped");
            broadcastUpdate("match_stop", Map.of("timestamp", System.currentTimeMillis()));
        }
        ctx.json(Map.of("success", true, "message", "Match stopped"));
    }

    private void resetMatch(Context ctx) {
        matchRunning = false;
        matchStartTime = 0;
        robotManager.stopTeleop();
        logger.info("Match reset");
        broadcastUpdate("match_reset", Map.of("timestamp", System.currentTimeMillis()));
        ctx.json(Map.of("success", true, "message", "Match reset"));
    }

    private void setMatchDuration(Context ctx) {
        try {
            long durationSeconds = Long.parseLong(ctx.body());
            matchDurationMs = durationSeconds * 1000;

            // If match is not running, reset the timer to new duration
            if (!matchRunning) {
                matchStartTime = 0;
            }

            logger.info("Match duration set to {} seconds", durationSeconds);
            broadcastUpdate("match_duration_changed", Map.of("durationMs", matchDurationMs));

            // Broadcast updated timer state
            broadcastUpdate("timer_update", Map.of(
                "timeRemainingMs", matchDurationMs,
                "timeRemainingSeconds", durationSeconds,
                "running", matchRunning
            ));

            ctx.json(Map.of("success", true, "durationMs", matchDurationMs));
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "Invalid duration format"));
        }
    }

    private void startTimerBroadcast() {
        // Broadcast timer updates every second
        timerExecutor.scheduleAtFixedRate(() -> {
            if (matchRunning) {
                long now = System.currentTimeMillis();
                long timeRemainingMs = Math.max(0, matchDurationMs - (now - matchStartTime));

                // Auto-stop when time expires
                if (timeRemainingMs == 0 && matchRunning) {
                    matchRunning = false;
                    robotManager.stopTeleop();
                    broadcastUpdate("match_end", Map.of("timestamp", now));
                    logger.info("Match ended - time expired");
                }

                broadcastUpdate("timer_update", Map.of(
                    "timeRemainingMs", timeRemainingMs,
                    "timeRemainingSeconds", timeRemainingMs / 1000,
                    "running", matchRunning
                ));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void startControllerMonitoring() {
        // Monitor controller count and broadcast updates when it changes
        timerExecutor.scheduleAtFixedRate(() -> {
            try {
                int currentCount = controllerManager.getConnectedControllerCount();
                if (currentCount != lastControllerCount) {
                    logger.debug("Controller count changed: {} -> {}", lastControllerCount, currentCount);
                    lastControllerCount = currentCount;
                    long timestamp = System.currentTimeMillis();
                    broadcastUpdate("controllers_updated", Map.of(
                        "count", currentCount,
                        "timestamp", timestamp
                    ));
                }
            } catch (Exception e) {
                logger.error("Error monitoring controllers", e);
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    public void start() {
        start(DEFAULT_PORT);
    }

    public void start(int port) {
        app.start(port);
        logger.info("API server started on port {}", port);
    }

    public void stop() {
        if (timerExecutor != null) {
            timerExecutor.shutdown();
            try {
                if (!timerExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    timerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                timerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        app.stop();
        logger.info("API server stopped");
    }
}

package com.soccerbots.control.robot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccerbots.control.network.NetworkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RobotManager {
    private static final Logger logger = LoggerFactory.getLogger(RobotManager.class);
    
    private static final int DISCOVERY_PORT = 12345;
    private static final int COMMAND_PORT = 12346;
    private static final String DISCOVERY_MESSAGE = "SOCCERBOTS_DISCOVERY";
    
    private final NetworkManager networkManager;
    private final ObjectMapper objectMapper;
    private final Map<String, Robot> connectedRobots;
    private final ExecutorService executorService;
    private final AtomicBoolean isDiscovering;
    
    private DatagramSocket discoverySocket;
    private DatagramSocket commandSocket;
    
    public RobotManager(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.objectMapper = new ObjectMapper();
        this.connectedRobots = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.isDiscovering = new AtomicBoolean(false);
        
        initializeSockets();
        startDiscoveryListener();
    }
    
    private void initializeSockets() {
        try {
            discoverySocket = networkManager.createUDPSocket(DISCOVERY_PORT);
            commandSocket = networkManager.createUDPSocket(COMMAND_PORT);
            logger.info("Initialized sockets on ports {} and {}", DISCOVERY_PORT, COMMAND_PORT);
        } catch (SocketException e) {
            logger.error("Failed to initialize sockets", e);
        }
    }
    
    private void startDiscoveryListener() {
        executorService.submit(() -> {
            byte[] buffer = new byte[1024];
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    discoverySocket.receive(packet);
                    
                    String message = new String(packet.getData(), 0, packet.getLength());
                    String senderIP = packet.getAddress().getHostAddress();
                    
                    handleDiscoveryResponse(message, senderIP);
                    
                } catch (Exception e) {
                    if (!Thread.currentThread().isInterrupted()) {
                        logger.error("Error in discovery listener", e);
                    }
                }
            }
        });
    }
    
    private void handleDiscoveryResponse(String message, String senderIP) {
        try {
            if (message.startsWith("SOCCERBOTS_ROBOT:")) {
                String robotData = message.substring("SOCCERBOTS_ROBOT:".length());
                RobotInfo robotInfo = objectMapper.readValue(robotData, RobotInfo.class);
                
                Robot robot = new Robot(robotInfo.getId(), robotInfo.getName(), senderIP, robotInfo.getStatus());
                connectedRobots.put(robot.getId(), robot);
                
                logger.info("Discovered robot: {} at {}", robot.getName(), senderIP);
                
                sendRobotResponse(senderIP, "DISCOVERY_ACK");
            }
        } catch (Exception e) {
            logger.error("Failed to handle discovery response", e);
        }
    }
    
    public void startDiscovery() {
        if (isDiscovering.get()) {
            return;
        }
        
        isDiscovering.set(true);
        logger.info("Starting robot discovery");
        
        executorService.submit(() -> {
            try {
                String broadcastAddress = getBroadcastAddress();
                if (broadcastAddress != null) {
                    
                    for (int i = 0; i < 3; i++) {
                        networkManager.sendUDPMessage(DISCOVERY_MESSAGE, broadcastAddress, DISCOVERY_PORT);
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                logger.error("Error during discovery", e);
            } finally {
                isDiscovering.set(false);
            }
        });
    }
    
    private String getBroadcastAddress() {
        try {
            for (NetworkInterface networkInterface : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) {
                        return broadcast.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get broadcast address", e);
        }
        return "255.255.255.255";
    }
    
    private void sendRobotResponse(String targetIP, String message) {
        networkManager.sendUDPMessage(message, targetIP, DISCOVERY_PORT);
    }
    
    public void sendCommand(String robotId, RobotCommand command) {
        Robot robot = connectedRobots.get(robotId);
        if (robot == null) {
            logger.warn("Robot not found: {}", robotId);
            return;
        }
        
        executorService.submit(() -> {
            try {
                String commandJson = objectMapper.writeValueAsString(command);
                networkManager.sendUDPMessage(commandJson, robot.getIpAddress(), COMMAND_PORT);
                robot.updateLastCommandTime();
                
                logger.debug("Sent command to robot {}: {}", robotId, command.getType());
            } catch (Exception e) {
                logger.error("Failed to send command to robot {}", robotId, e);
            }
        });
    }
    
    public void sendMovementCommand(String robotId, double forward, double sideways, double rotation) {
        RobotCommand command = new RobotCommand("MOVE");
        command.addParameter("forward", forward);
        command.addParameter("sideways", sideways);
        command.addParameter("rotation", rotation);
        command.addParameter("timestamp", System.currentTimeMillis());
        
        sendCommand(robotId, command);
    }
    
    public void sendStopCommand(String robotId) {
        RobotCommand command = new RobotCommand("STOP");
        command.addParameter("timestamp", System.currentTimeMillis());
        
        sendCommand(robotId, command);
    }
    
    public void configureRobotWiFi(String robotId, String ssid, String password) {
        RobotCommand command = new RobotCommand("CONFIGURE_WIFI");
        command.addParameter("ssid", ssid);
        command.addParameter("password", password);
        command.addParameter("timestamp", System.currentTimeMillis());
        
        sendCommand(robotId, command);
    }
    
    public List<Robot> getConnectedRobots() {
        return new ArrayList<>(connectedRobots.values());
    }
    
    public int getConnectedRobotCount() {
        return connectedRobots.size();
    }
    
    public Robot getRobot(String robotId) {
        return connectedRobots.get(robotId);
    }
    
    public void removeRobot(String robotId) {
        Robot removed = connectedRobots.remove(robotId);
        if (removed != null) {
            logger.info("Removed robot: {}", removed.getName());
        }
    }
    
    public void clearOfflineRobots() {
        long currentTime = System.currentTimeMillis();
        long timeout = 30000; // 30 seconds
        
        connectedRobots.entrySet().removeIf(entry -> {
            Robot robot = entry.getValue();
            boolean isOffline = (currentTime - robot.getLastSeenTime()) > timeout;
            if (isOffline) {
                logger.info("Removing offline robot: {}", robot.getName());
            }
            return isOffline;
        });
    }
    
    public void shutdown() {
        logger.info("Shutting down robot manager");
        
        if (discoverySocket != null && !discoverySocket.isClosed()) {
            discoverySocket.close();
        }
        if (commandSocket != null && !commandSocket.isClosed()) {
            commandSocket.close();
        }
        
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
    
    public static class RobotInfo {
        private String id;
        private String name;
        private String status;
        
        public RobotInfo() {}
        
        public RobotInfo(String id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
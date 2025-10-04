package com.soccerbots.control.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {
    private static final Logger logger = LoggerFactory.getLogger(NetworkManager.class);

    // ESP32 Communication Constants
    public static final int DISCOVERY_PORT = 12345;
    public static final int COMMAND_PORT_BASE = 12346;
    public static final int ESP32_UDP_PORT = 2367; // Legacy port
    public static final String EXPECTED_WIFI_NETWORK = "WATCHTOWER";

    private boolean isConnectedToNetwork = false;
    private String currentSSID = "";
    private ExecutorService executorService;
    private DatagramSocket udpSocket;
    private DatagramSocket discoverySocket;

    public NetworkManager() {
        this.executorService = Executors.newCachedThreadPool();
        checkCurrentNetworkStatus();
        initializeUDPSocket();
        initializeDiscoverySocket();
    }

    private void initializeUDPSocket() {
        try {
            udpSocket = new DatagramSocket();
            logger.info("UDP socket initialized for ESP32 communication");
        } catch (SocketException e) {
            logger.error("Failed to initialize UDP socket", e);
        }
    }

    private void initializeDiscoverySocket() {
        try {
            discoverySocket = new DatagramSocket(DISCOVERY_PORT);
            discoverySocket.setSoTimeout(100); // 100ms timeout for non-blocking receives
            logger.info("Discovery socket initialized on port {}", DISCOVERY_PORT);
        } catch (SocketException e) {
            logger.error("Failed to initialize discovery socket", e);
        }
    }

    private void checkCurrentNetworkStatus() {
        try {
            Process process = Runtime.getRuntime().exec("netsh wlan show interfaces");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));

            String line;
            boolean connected = false;
            String ssid = "";

            while ((line = reader.readLine()) != null) {
                if (line.contains("State") && line.contains("connected")) {
                    connected = true;
                } else if (line.contains("SSID") && !line.contains("BSSID")) {
                    ssid = line.split(":")[1].trim();
                }
            }

            isConnectedToNetwork = connected;
            currentSSID = ssid;

            process.waitFor();

            if (connected) {
                logger.info("Connected to WiFi network: {}", ssid);
                if (EXPECTED_WIFI_NETWORK.equals(ssid)) {
                    logger.info("Connected to expected ESP32 network!");
                } else {
                    logger.warn("Connected to network '{}', but ESP32 robots expect '{}'",
                              ssid, EXPECTED_WIFI_NETWORK);
                }
            } else {
                logger.warn("Not connected to any WiFi network");
            }

        } catch (Exception e) {
            logger.error("Failed to check network status", e);
        }
    }

    /**
     * Send binary command data to ESP32 robot
     * Format: robotName(16 bytes) + axes(6 bytes) + buttons(2 bytes)
     */
    public void sendRobotCommand(String robotName, String targetIP,
                                int leftX, int leftY, int rightX, int rightY,
                                boolean cross, boolean circle, boolean square, boolean triangle) {
        executorService.submit(() -> {
            try {
                // Create 24-byte packet: 16 bytes name + 6 bytes axes + 2 bytes buttons
                byte[] packet = new byte[24];

                // Robot name (16 bytes, null-padded)
                byte[] nameBytes = robotName.getBytes();
                System.arraycopy(nameBytes, 0, packet, 0, Math.min(nameBytes.length, 16));

                // Axes data (6 bytes) - convert from float to 0-255 range
                packet[16] = (byte) Math.max(0, Math.min(255, leftX));   // leftX
                packet[17] = (byte) Math.max(0, Math.min(255, leftY));   // leftY
                packet[18] = (byte) Math.max(0, Math.min(255, rightX));  // rightX
                packet[19] = (byte) Math.max(0, Math.min(255, rightY));  // rightY
                packet[20] = (byte) 125; // unused axis
                packet[21] = (byte) 125; // unused axis

                // Button data (2 bytes)
                byte button1 = 0;
                if (cross) button1 |= 0x01;
                if (circle) button1 |= 0x02;
                if (square) button1 |= 0x04;
                if (triangle) button1 |= 0x08;

                packet[22] = button1;
                packet[23] = 0; // unused buttons

                // Send UDP packet
                DatagramPacket udpPacket = new DatagramPacket(
                    packet, packet.length,
                    InetAddress.getByName(targetIP), ESP32_UDP_PORT
                );
                udpSocket.send(udpPacket);

                logger.debug("Sent command to robot '{}' at {}:{}", robotName, targetIP, ESP32_UDP_PORT);

            } catch (Exception e) {
                logger.error("Failed to send robot command to " + targetIP, e);
            }
        });
    }

    /**
     * Send game status command to ESP32 robot
     * Format: "robotName:status" (text)
     */
    public void sendGameStatus(String robotName, String targetIP, String status) {
        executorService.submit(() -> {
            try {
                String message = robotName + ":" + status;
                byte[] data = message.getBytes();

                DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    InetAddress.getByName(targetIP), ESP32_UDP_PORT
                );
                udpSocket.send(packet);

                logger.info("Sent game status '{}' to robot '{}' at {}", status, robotName, targetIP);

            } catch (Exception e) {
                logger.error("Failed to send game status to " + targetIP, e);
            }
        });
    }

    /**
     * Broadcast game status to all robots on the network
     */
    public void broadcastGameStatus(String status) {
        try {
            // Get network info to determine broadcast address
            String subnet = getNetworkSubnet();
            if (subnet != null) {
                String broadcastAddr = subnet + ".255";
                logger.info("Broadcasting game status '{}' to {}", status, broadcastAddr);

                // Send to broadcast address - robots will filter by their name
                sendGameStatus("ALL", broadcastAddr, status);
            }
        } catch (Exception e) {
            logger.error("Failed to broadcast game status", e);
        }
    }

    private String getNetworkSubnet() {
        try {
            Process process = Runtime.getRuntime().exec("ipconfig");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("IPv4 Address") && line.contains("192.168")) {
                    String ip = line.split(":")[1].trim();
                    String[] parts = ip.split("\\.");
                    if (parts.length >= 3) {
                        return parts[0] + "." + parts[1] + "." + parts[2];
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            logger.error("Failed to get network subnet", e);
        }
        return null;
    }

    public boolean isNetworkActive() {
        checkCurrentNetworkStatus(); // Refresh status
        return isConnectedToNetwork;
    }

    public boolean isConnectedToExpectedNetwork() {
        return isConnectedToNetwork && EXPECTED_WIFI_NETWORK.equals(currentSSID);
    }

    public String getCurrentSSID() {
        return currentSSID;
    }

    public String getExpectedNetwork() {
        return EXPECTED_WIFI_NETWORK;
    }

    /**
     * Receive discovery message from robot
     * Returns null if no message available (non-blocking)
     */
    public String receiveDiscoveryMessage() {
        try {
            byte[] buffer = new byte[256];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            discoverySocket.receive(packet);
            String message = new String(packet.getData(), 0, packet.getLength());
            logger.debug("Received discovery message: {}", message);
            return message;
        } catch (SocketTimeoutException e) {
            // No message available, this is normal
            return null;
        } catch (Exception e) {
            logger.error("Error receiving discovery message", e);
            return null;
        }
    }

    /**
     * Send discovery response to robot
     */
    public void sendDiscoveryResponse(String targetIP, String message) {
        executorService.submit(() -> {
            try {
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    InetAddress.getByName(targetIP), DISCOVERY_PORT
                );
                udpSocket.send(packet);
                logger.debug("Sent discovery response to {}: {}", targetIP, message);
            } catch (Exception e) {
                logger.error("Failed to send discovery response to " + targetIP, e);
            }
        });
    }

    /**
     * Send emergency stop to robot
     */
    public void sendEmergencyStop(String targetIP) {
        executorService.submit(() -> {
            try {
                String message = "ESTOP";
                byte[] data = message.getBytes();

                // Send to discovery port (robot always listens here when connected)
                DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    InetAddress.getByName(targetIP), DISCOVERY_PORT
                );
                udpSocket.send(packet);

                logger.info("Sent emergency stop to {}", targetIP);
            } catch (Exception e) {
                logger.error("Failed to send emergency stop to " + targetIP, e);
            }
        });
    }

    /**
     * Send emergency stop release to robot
     */
    public void sendEmergencyStopRelease(String targetIP) {
        executorService.submit(() -> {
            try {
                String message = "ESTOP_OFF";
                byte[] data = message.getBytes();

                DatagramPacket packet = new DatagramPacket(
                    data, data.length,
                    InetAddress.getByName(targetIP), DISCOVERY_PORT
                );
                udpSocket.send(packet);

                logger.info("Sent emergency stop release to {}", targetIP);
            } catch (Exception e) {
                logger.error("Failed to send emergency stop release to " + targetIP, e);
            }
        });
    }

    public void shutdown() {
        logger.info("Shutting down network manager");
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
        if (discoverySocket != null && !discoverySocket.isClosed()) {
            discoverySocket.close();
        }
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
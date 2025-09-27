package com.soccerbots.control.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NetworkManager {
    private static final Logger logger = LoggerFactory.getLogger(NetworkManager.class);
    
    private boolean isHostingNetwork = false;
    private boolean isConnectedToNetwork = false;
    private String currentSSID = "";
    private ExecutorService executorService;
    private Process hostedNetworkProcess;
    
    public NetworkManager() {
        this.executorService = Executors.newCachedThreadPool();
        checkCurrentNetworkStatus();
    }
    
    public boolean startHostedNetwork(String ssid, String password) {
        try {
            stopHostedNetwork();
            
            logger.info("Starting hosted network: {}", ssid);
            
            String[] commands = {
                "netsh wlan set hostednetwork mode=allow ssid=" + ssid + " key=" + password,
                "netsh wlan start hostednetwork"
            };
            
            for (String command : commands) {
                Process process = Runtime.getRuntime().exec(command);
                int result = process.waitFor();
                if (result != 0) {
                    logger.error("Failed to execute command: {}", command);
                    return false;
                }
            }
            
            isHostingNetwork = true;
            currentSSID = ssid;
            logger.info("Hosted network started successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to start hosted network", e);
            return false;
        }
    }
    
    public boolean stopHostedNetwork() {
        try {
            if (hostedNetworkProcess != null && hostedNetworkProcess.isAlive()) {
                hostedNetworkProcess.destroyForcibly();
            }
            
            Process process = Runtime.getRuntime().exec("netsh wlan stop hostednetwork");
            int result = process.waitFor();
            
            isHostingNetwork = false;
            logger.info("Hosted network stopped");
            return result == 0;
            
        } catch (Exception e) {
            logger.error("Failed to stop hosted network", e);
            return false;
        }
    }
    
    public boolean connectToNetwork(String ssid, String password) {
        try {
            logger.info("Connecting to network: {}", ssid);
            
            String profileXml = createWiFiProfile(ssid, password);
            String tempFile = System.getProperty("java.io.tmpdir") + "\\temp_wifi_profile.xml";
            
            java.nio.file.Files.write(java.nio.file.Paths.get(tempFile), profileXml.getBytes());
            
            String[] commands = {
                "netsh wlan add profile filename=\"" + tempFile + "\"",
                "netsh wlan connect name=\"" + ssid + "\""
            };
            
            for (String command : commands) {
                Process process = Runtime.getRuntime().exec(command);
                int result = process.waitFor();
                if (result != 0) {
                    logger.error("Failed to execute command: {}", command);
                    return false;
                }
            }
            
            Thread.sleep(3000);
            checkCurrentNetworkStatus();
            
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(tempFile));
            
            logger.info("Connected to network successfully");
            return isConnectedToNetwork;
            
        } catch (Exception e) {
            logger.error("Failed to connect to network", e);
            return false;
        }
    }
    
    private String createWiFiProfile(String ssid, String password) {
        return "<?xml version=\"1.0\"?>\n" +
               "<WLANProfile xmlns=\"http://www.microsoft.com/networking/WLAN/profile/v1\">\n" +
               "    <name>" + ssid + "</name>\n" +
               "    <SSIDConfig>\n" +
               "        <SSID>\n" +
               "            <name>" + ssid + "</name>\n" +
               "        </SSID>\n" +
               "    </SSIDConfig>\n" +
               "    <connectionType>ESS</connectionType>\n" +
               "    <connectionMode>auto</connectionMode>\n" +
               "    <MSM>\n" +
               "        <security>\n" +
               "            <authEncryption>\n" +
               "                <authentication>WPA2PSK</authentication>\n" +
               "                <encryption>AES</encryption>\n" +
               "                <useOneX>false</useOneX>\n" +
               "            </authEncryption>\n" +
               "            <sharedKey>\n" +
               "                <keyType>passPhrase</keyType>\n" +
               "                <protected>false</protected>\n" +
               "                <keyMaterial>" + password + "</keyMaterial>\n" +
               "            </sharedKey>\n" +
               "        </security>\n" +
               "    </MSM>\n" +
               "</WLANProfile>";
    }
    
    public List<String> scanAvailableNetworks() {
        List<String> networks = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("netsh wlan show profiles");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("All User Profile")) {
                    String ssid = line.split(":")[1].trim();
                    networks.add(ssid);
                }
            }
            
            process.waitFor();
            logger.info("Found {} available networks", networks.size());
            
        } catch (Exception e) {
            logger.error("Failed to scan networks", e);
        }
        return networks;
    }
    
    private void checkCurrentNetworkStatus() {
        try {
            Process process = Runtime.getRuntime().exec("netsh wlan show interfaces");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
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
            
        } catch (Exception e) {
            logger.error("Failed to check network status", e);
        }
    }
    
    public DatagramSocket createUDPSocket(int port) throws SocketException {
        return new DatagramSocket(port);
    }
    
    public void sendUDPMessage(String message, String targetIP, int targetPort) {
        executorService.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(
                    data, data.length, 
                    InetAddress.getByName(targetIP), targetPort
                );
                socket.send(packet);
                logger.debug("Sent UDP message to {}:{}", targetIP, targetPort);
            } catch (Exception e) {
                logger.error("Failed to send UDP message", e);
            }
        });
    }
    
    public boolean isNetworkActive() {
        return isHostingNetwork || isConnectedToNetwork;
    }
    
    public boolean isHostingNetwork() {
        return isHostingNetwork;
    }
    
    public boolean isConnectedToNetwork() {
        return isConnectedToNetwork;
    }
    
    public String getCurrentSSID() {
        return currentSSID;
    }
    
    public void shutdown() {
        logger.info("Shutting down network manager");
        if (isHostingNetwork) {
            stopHostedNetwork();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
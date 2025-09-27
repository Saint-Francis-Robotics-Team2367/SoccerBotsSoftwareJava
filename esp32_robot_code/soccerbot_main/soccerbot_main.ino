/*
 * SoccerBot ESP32 Main Code
 * 
 * This code runs on ESP32-WROOM modules for soccer robot control.
 * Features:
 * - WiFi connectivity with auto-reconnection
 * - Bluetooth for configuration
 * - UDP command receiving and parsing
 * - LED status indicators
 * - Low-latency command processing
 * 
 * Author: SoccerBots Development Team
 * Version: 1.0.0
 */

#include <WiFi.h>
#include <WiFiUdp.h>
#include <BluetoothSerial.h>
#include <ArduinoJson.h>
#include <Preferences.h>
#include <esp_wifi.h>

// Pin definitions for ESP32-WROOM
#define LED_STATUS_PIN 2      // Built-in LED
#define LED_WIFI_PIN 4        // WiFi status LED
#define LED_BT_PIN 5          // Bluetooth status LED
#define LED_COMMAND_PIN 18    // Command received LED

// Network configuration
#define UDP_DISCOVERY_PORT 12345
#define UDP_COMMAND_PORT 12346
#define DISCOVERY_TIMEOUT 30000
#define COMMAND_TIMEOUT 5000
#define HEARTBEAT_INTERVAL 10000

// Robot configuration - CHANGE THESE FOR EACH ROBOT
const char* ROBOT_TEAM_NAME = "Team_Alpha";    // Change this for each team
const char* ROBOT_DEFAULT_NAME = "SoccerBot_01";  // Change this for each robot
const char* ROBOT_ID = "ROBOT_001";           // Unique ID for each robot

// Global variables
WiFiUDP udpDiscovery;
WiFiUDP udpCommand;
BluetoothSerial SerialBT;
Preferences preferences;

// Robot state
String robotName = ROBOT_DEFAULT_NAME;
String robotStatus = "IDLE";
bool wifiConnected = false;
bool bluetoothEnabled = false;
bool discoveryMode = false;
unsigned long lastCommandTime = 0;
unsigned long lastHeartbeat = 0;
unsigned long lastDiscoveryResponse = 0;

// WiFi credentials (stored in preferences)
String savedSSID = "";
String savedPassword = "";

// Command structure
struct RobotCommand {
  String type;
  double forward;
  double sideways;
  double rotation;
  unsigned long timestamp;
  bool valid;
};

void setup() {
  Serial.begin(115200);
  delay(1000);
  
  Serial.println("=== SoccerBot ESP32 Starting ===");
  Serial.printf("Robot ID: %s\n", ROBOT_ID);
  Serial.printf("Team: %s\n", ROBOT_TEAM_NAME);
  Serial.printf("Name: %s\n", robotName.c_str());
  
  initializePins();
  initializePreferences();
  loadConfiguration();
  initializeBluetooth();
  initializeWiFi();
  
  Serial.println("=== SoccerBot Ready ===");
  updateStatusLEDs();
}

void loop() {
  handleWiFiConnection();
  handleBluetoothConfig();
  handleDiscovery();
  handleCommands();
  handleHeartbeat();
  updateStatusLEDs();
  
  delay(10); // Small delay for stability
}

void initializePins() {
  pinMode(LED_STATUS_PIN, OUTPUT);
  pinMode(LED_WIFI_PIN, OUTPUT);
  pinMode(LED_BT_PIN, OUTPUT);
  pinMode(LED_COMMAND_PIN, OUTPUT);
  
  // Initial LED test
  digitalWrite(LED_STATUS_PIN, HIGH);
  digitalWrite(LED_WIFI_PIN, HIGH);
  digitalWrite(LED_BT_PIN, HIGH);
  digitalWrite(LED_COMMAND_PIN, HIGH);
  delay(500);
  digitalWrite(LED_STATUS_PIN, LOW);
  digitalWrite(LED_WIFI_PIN, LOW);
  digitalWrite(LED_BT_PIN, LOW);
  digitalWrite(LED_COMMAND_PIN, LOW);
  
  Serial.println("Pins initialized");
}

void initializePreferences() {
  preferences.begin("soccerbot", false);
  
  // Load robot name if saved
  String storedName = preferences.getString("robotName", "");
  if (storedName.length() > 0) {
    robotName = storedName;
  } else {
    // Set default name based on team and robot ID
    if (strlen(ROBOT_TEAM_NAME) > 0) {
      robotName = String(ROBOT_TEAM_NAME) + "_" + String(ROBOT_ID);
    }
    preferences.putString("robotName", robotName);
  }
  
  Serial.printf("Robot name: %s\n", robotName.c_str());
}

void loadConfiguration() {
  savedSSID = preferences.getString("wifi_ssid", "");
  savedPassword = preferences.getString("wifi_password", "");
  
  Serial.printf("Loaded WiFi config - SSID: %s\n", 
                savedSSID.length() > 0 ? savedSSID.c_str() : "Not configured");
}

void initializeBluetooth() {
  String btName = "SoccerBot_" + String(ROBOT_ID);
  
  if (SerialBT.begin(btName)) {
    bluetoothEnabled = true;
    Serial.printf("Bluetooth initialized: %s\n", btName.c_str());
  } else {
    Serial.println("Bluetooth initialization failed");
  }
}

void initializeWiFi() {
  WiFi.mode(WIFI_STA);
  WiFi.setAutoReconnect(true);
  
  if (savedSSID.length() > 0) {
    connectToWiFi(savedSSID, savedPassword);
  } else {
    Serial.println("No WiFi credentials stored");
    robotStatus = "NO_WIFI_CONFIG";
  }
}

void connectToWiFi(String ssid, String password) {
  Serial.printf("Connecting to WiFi: %s\n", ssid.c_str());
  robotStatus = "CONNECTING_WIFI";
  
  WiFi.begin(ssid.c_str(), password.c_str());
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
    updateStatusLEDs();
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    wifiConnected = true;
    robotStatus = "CONNECTED";
    Serial.printf("\nWiFi connected! IP: %s\n", WiFi.localIP().toString().c_str());
    
    // Start UDP services
    udpDiscovery.begin(UDP_DISCOVERY_PORT);
    udpCommand.begin(UDP_COMMAND_PORT);
    
    // Enter discovery mode
    discoveryMode = true;
    
  } else {
    wifiConnected = false;
    robotStatus = "WIFI_FAILED";
    Serial.println("\nWiFi connection failed");
  }
}

void handleWiFiConnection() {
  if (WiFi.status() != WL_CONNECTED && wifiConnected) {
    wifiConnected = false;
    robotStatus = "WIFI_DISCONNECTED";
    Serial.println("WiFi disconnected");
  } else if (WiFi.status() == WL_CONNECTED && !wifiConnected) {
    wifiConnected = true;
    robotStatus = "CONNECTED";
    Serial.printf("WiFi reconnected! IP: %s\n", WiFi.localIP().toString().c_str());
    
    // Restart UDP services
    udpDiscovery.begin(UDP_DISCOVERY_PORT);
    udpCommand.begin(UDP_COMMAND_PORT);
    discoveryMode = true;
  }
}

void handleBluetoothConfig() {
  if (!bluetoothEnabled) return;
  
  if (SerialBT.available()) {
    String btMessage = SerialBT.readString();
    btMessage.trim();
    
    Serial.printf("Bluetooth message: %s\n", btMessage.c_str());
    
    // Parse JSON configuration
    DynamicJsonDocument doc(1024);
    DeserializationError error = deserializeJson(doc, btMessage);
    
    if (error) {
      SerialBT.println("{\"status\":\"error\",\"message\":\"Invalid JSON\"}");
      return;
    }
    
    String command = doc["command"];
    
    if (command == "configure_wifi") {
      String ssid = doc["ssid"];
      String password = doc["password"];
      
      if (ssid.length() > 0 && password.length() >= 8) {
        // Save credentials
        preferences.putString("wifi_ssid", ssid);
        preferences.putString("wifi_password", password);
        
        savedSSID = ssid;
        savedPassword = password;
        
        SerialBT.println("{\"status\":\"success\",\"message\":\"WiFi configured, restarting...\"}");
        
        delay(1000);
        ESP.restart();
        
      } else {
        SerialBT.println("{\"status\":\"error\",\"message\":\"Invalid SSID or password\"}");
      }
      
    } else if (command == "get_status") {
      DynamicJsonDocument statusDoc(512);
      statusDoc["robot_id"] = ROBOT_ID;
      statusDoc["name"] = robotName;
      statusDoc["status"] = robotStatus;
      statusDoc["wifi_connected"] = wifiConnected;
      statusDoc["ip_address"] = WiFi.localIP().toString();
      statusDoc["wifi_ssid"] = WiFi.SSID();
      
      String response;
      serializeJson(statusDoc, response);
      SerialBT.println(response);
      
    } else if (command == "set_name") {
      String newName = doc["name"];
      if (newName.length() > 0) {
        robotName = newName;
        preferences.putString("robotName", robotName);
        SerialBT.println("{\"status\":\"success\",\"message\":\"Name updated\"}");
      } else {
        SerialBT.println("{\"status\":\"error\",\"message\":\"Invalid name\"}");
      }
    }
  }
}

void handleDiscovery() {
  if (!wifiConnected || !discoveryMode) return;
  
  int packetSize = udpDiscovery.parsePacket();
  if (packetSize) {
    char packetBuffer[255];
    int len = udpDiscovery.read(packetBuffer, 254);
    packetBuffer[len] = '\0';
    
    String message = String(packetBuffer);
    
    if (message == "SOCCERBOTS_DISCOVERY") {
      // Respond with robot information
      DynamicJsonDocument doc(512);
      doc["id"] = ROBOT_ID;
      doc["name"] = robotName;
      doc["status"] = robotStatus;
      doc["ip"] = WiFi.localIP().toString();
      doc["team"] = ROBOT_TEAM_NAME;
      
      String response = "SOCCERBOTS_ROBOT:";
      String jsonResponse;
      serializeJson(doc, jsonResponse);
      response += jsonResponse;
      
      udpDiscovery.beginPacket(udpDiscovery.remoteIP(), udpDiscovery.remotePort());
      udpDiscovery.print(response);
      udpDiscovery.endPacket();
      
      lastDiscoveryResponse = millis();
      
      Serial.printf("Discovery response sent to %s\n", udpDiscovery.remoteIP().toString().c_str());
      
      // Flash command LED to indicate discovery response
      digitalWrite(LED_COMMAND_PIN, HIGH);
      delay(50);
      digitalWrite(LED_COMMAND_PIN, LOW);
    }
  }
  
  // Exit discovery mode after timeout
  if (discoveryMode && (millis() - lastDiscoveryResponse) > DISCOVERY_TIMEOUT) {
    discoveryMode = false;
    Serial.println("Discovery mode disabled");
  }
}

void handleCommands() {
  if (!wifiConnected) return;
  
  int packetSize = udpCommand.parsePacket();
  if (packetSize) {
    char packetBuffer[512];
    int len = udpCommand.read(packetBuffer, 511);
    packetBuffer[len] = '\0';
    
    String commandJson = String(packetBuffer);
    RobotCommand cmd = parseCommand(commandJson);
    
    if (cmd.valid) {
      executeCommand(cmd);
      lastCommandTime = millis();
      
      // Flash command LED
      digitalWrite(LED_COMMAND_PIN, HIGH);
      delay(20);
      digitalWrite(LED_COMMAND_PIN, LOW);
      
      Serial.printf("Command executed: %s (%.2f, %.2f, %.2f)\n", 
                   cmd.type.c_str(), cmd.forward, cmd.sideways, cmd.rotation);
    } else {
      Serial.println("Invalid command received");
    }
  }
  
  // Check for command timeout
  if ((millis() - lastCommandTime) > COMMAND_TIMEOUT && lastCommandTime > 0) {
    // Stop robot if no commands received
    stopRobot();
  }
}

RobotCommand parseCommand(String jsonString) {
  RobotCommand cmd;
  cmd.valid = false;
  
  DynamicJsonDocument doc(512);
  DeserializationError error = deserializeJson(doc, jsonString);
  
  if (error) {
    Serial.println("Command JSON parse error");
    return cmd;
  }
  
  cmd.type = doc["type"].as<String>();
  cmd.timestamp = doc["parameters"]["timestamp"];
  
  if (cmd.type == "MOVE") {
    cmd.forward = doc["parameters"]["forward"];
    cmd.sideways = doc["parameters"]["sideways"];
    cmd.rotation = doc["parameters"]["rotation"];
    cmd.valid = true;
  } else if (cmd.type == "STOP") {
    cmd.forward = 0.0;
    cmd.sideways = 0.0;
    cmd.rotation = 0.0;
    cmd.valid = true;
  } else if (cmd.type == "CONFIGURE_WIFI") {
    // Handle WiFi configuration via UDP
    String ssid = doc["parameters"]["ssid"];
    String password = doc["parameters"]["password"];
    
    if (ssid.length() > 0 && password.length() >= 8) {
      preferences.putString("wifi_ssid", ssid);
      preferences.putString("wifi_password", password);
      
      Serial.println("WiFi reconfigured via UDP, restarting...");
      delay(1000);
      ESP.restart();
    }
  }
  
  return cmd;
}

void executeCommand(RobotCommand cmd) {
  if (cmd.type == "MOVE") {
    // This is where you would add motor control logic
    // For now, we just update the robot status
    robotStatus = "MOVING";
    
    // TODO: Implement motor control
    // Example:
    // setMotorSpeeds(cmd.forward, cmd.sideways, cmd.rotation);
    
    Serial.printf("MOVE command: F=%.2f, S=%.2f, R=%.2f\n", 
                 cmd.forward, cmd.sideways, cmd.rotation);
                 
  } else if (cmd.type == "STOP") {
    stopRobot();
  }
}

void stopRobot() {
  robotStatus = "STOPPED";
  
  // TODO: Implement motor stop logic
  // Example:
  // setMotorSpeeds(0.0, 0.0, 0.0);
  
  Serial.println("Robot stopped");
}

void handleHeartbeat() {
  if (wifiConnected && (millis() - lastHeartbeat) > HEARTBEAT_INTERVAL) {
    // Send heartbeat to maintain connection awareness
    robotStatus = "READY";
    lastHeartbeat = millis();
    
    // Re-enable discovery mode periodically
    if (!discoveryMode) {
      discoveryMode = true;
      Serial.println("Re-enabled discovery mode");
    }
  }
}

void updateStatusLEDs() {
  // Status LED (general system status)
  if (robotStatus == "CONNECTED" || robotStatus == "READY") {
    digitalWrite(LED_STATUS_PIN, HIGH);
  } else {
    // Blink for other states
    digitalWrite(LED_STATUS_PIN, (millis() / 500) % 2);
  }
  
  // WiFi LED
  digitalWrite(LED_WIFI_PIN, wifiConnected ? HIGH : LOW);
  
  // Bluetooth LED
  digitalWrite(LED_BT_PIN, bluetoothEnabled ? HIGH : LOW);
  
  // Command LED is handled in command processing
}

// TODO: Implement these functions for motor control
/*
void setMotorSpeeds(double forward, double sideways, double rotation) {
  // Calculate individual motor speeds for omnidirectional movement
  // This depends on your specific motor configuration
  
  // Example for 4-wheel omnidirectional drive:
  // double motor1 = forward + sideways + rotation;
  // double motor2 = forward - sideways - rotation;
  // double motor3 = forward - sideways + rotation;
  // double motor4 = forward + sideways - rotation;
  
  // Set motor PWM values here
}
*/
/*
 * SoccerBot ESP32 Main Code - Optimized
 *
 * Lightweight WiFi-only robot control for ESP32
 * Features:
 * - WiFi connectivity with auto-reconnection
 * - UDP command receiving and parsing
 * - LED status indicators
 * - Low-latency command processing
 *
 * Author: SoccerBots Development Team
 * Version: 1.1.0 (Optimized)
 */

#include <WiFi.h>
#include <WiFiUdp.h>
#include <ArduinoJson.h>
#include <Preferences.h>

// Pin definitions
#define LED_STATUS_PIN 2
#define LED_WIFI_PIN 4

// Network configuration
#define UDP_DISCOVERY_PORT 12345
#define UDP_COMMAND_PORT 12346
#define COMMAND_TIMEOUT 5000

// Robot configuration - CHANGE THESE FOR EACH ROBOT
const char* ROBOT_ID = "ROBOT_001";
const char* ROBOT_NAME = "SoccerBot_01";
const char* ROBOT_TEAM = "Team_Alpha";

// Default WiFi credentials - configure these for your network
const char* DEFAULT_SSID = "SoccerBots_Network";
const char* DEFAULT_PASSWORD = "soccerbots123";

// Global variables
WiFiUDP udp;
Preferences prefs;

// Robot state
bool wifiConnected = false;
unsigned long lastCommand = 0;

// Command structure
struct Command {
  double forward, sideways, rotation;
  bool valid;
};

void setup() {
  Serial.begin(115200);
  Serial.println("SoccerBot Starting...");

  initPins();
  initWiFi();

  Serial.println("SoccerBot Ready");
}

void loop() {
  checkWiFi();
  handleDiscovery();
  handleCommands();
  updateLEDs();
  delay(5);
}

void initPins() {
  pinMode(LED_STATUS_PIN, OUTPUT);
  pinMode(LED_WIFI_PIN, OUTPUT);

  // LED test
  digitalWrite(LED_STATUS_PIN, HIGH);
  digitalWrite(LED_WIFI_PIN, HIGH);
  delay(200);
  digitalWrite(LED_STATUS_PIN, LOW);
  digitalWrite(LED_WIFI_PIN, LOW);
}

void initWiFi() {
  WiFi.mode(WIFI_STA);
  WiFi.setAutoReconnect(true);

  // Try to load saved credentials
  prefs.begin("soccerbot", true);
  String ssid = prefs.getString("ssid", DEFAULT_SSID);
  String pass = prefs.getString("pass", DEFAULT_PASSWORD);
  prefs.end();

  connectWiFi(ssid, pass);
}

void connectWiFi(String ssid, String password) {
  Serial.printf("Connecting to: %s\n", ssid.c_str());
  WiFi.begin(ssid.c_str(), password.c_str());

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    wifiConnected = true;
    Serial.printf("\nConnected! IP: %s\n", WiFi.localIP().toString().c_str());
    udp.begin(UDP_DISCOVERY_PORT);
  } else {
    Serial.println("\nConnection failed");
  }
}

void checkWiFi() {
  bool connected = (WiFi.status() == WL_CONNECTED);

  if (connected != wifiConnected) {
    wifiConnected = connected;
    if (connected) {
      Serial.println("WiFi reconnected");
      udp.begin(UDP_DISCOVERY_PORT);
    } else {
      Serial.println("WiFi disconnected");
    }
  }
}

void handleDiscovery() {
  if (!wifiConnected) return;

  int size = udp.parsePacket();
  if (size && udp.remotePort() == UDP_DISCOVERY_PORT) {
    char buffer[64];
    udp.read(buffer, sizeof(buffer));

    if (strcmp(buffer, "SOCCERBOTS_DISCOVERY") == 0) {
      // Create compact response
      String response = "SOCCERBOTS_ROBOT:{\"id\":\"" + String(ROBOT_ID) +
                       "\",\"name\":\"" + String(ROBOT_NAME) +
                       "\",\"ip\":\"" + WiFi.localIP().toString() +
                       "\",\"team\":\"" + String(ROBOT_TEAM) + "\"}";

      udp.beginPacket(udp.remoteIP(), udp.remotePort());
      udp.print(response);
      udp.endPacket();

      Serial.println("Discovery response sent");
      flashLED(LED_STATUS_PIN, 50);
    }
  }
}

void handleCommands() {
  if (!wifiConnected) return;

  // Switch to command port for receiving commands
  udp.stop();
  udp.begin(UDP_COMMAND_PORT);

  int size = udp.parsePacket();
  if (size) {
    char buffer[256];
    int len = udp.read(buffer, sizeof(buffer) - 1);
    buffer[len] = '\0';

    Command cmd = parseCommand(buffer);
    if (cmd.valid) {
      executeCommand(cmd);
      lastCommand = millis();
      flashLED(LED_STATUS_PIN, 20);

      Serial.printf("CMD: F=%.2f S=%.2f R=%.2f\n", cmd.forward, cmd.sideways, cmd.rotation);
    }
  }

  // Check timeout and stop robot
  if (lastCommand > 0 && (millis() - lastCommand) > COMMAND_TIMEOUT) {
    stopRobot();
  }

  // Switch back to discovery port
  udp.stop();
  udp.begin(UDP_DISCOVERY_PORT);
}

Command parseCommand(const char* json) {
  Command cmd = {0, 0, 0, false};

  StaticJsonDocument<256> doc;
  if (deserializeJson(doc, json) != DeserializationError::Ok) {
    return cmd;
  }

  const char* type = doc["type"];
  if (!type) return cmd;

  if (strcmp(type, "MOVE") == 0) {
    JsonObject params = doc["parameters"];
    cmd.forward = params["forward"] | 0.0;
    cmd.sideways = params["sideways"] | 0.0;
    cmd.rotation = params["rotation"] | 0.0;
    cmd.valid = true;
  } else if (strcmp(type, "STOP") == 0) {
    cmd.valid = true; // All values already 0
  } else if (strcmp(type, "CONFIGURE_WIFI") == 0) {
    // WiFi reconfiguration
    JsonObject params = doc["parameters"];
    const char* ssid = params["ssid"];
    const char* pass = params["password"];

    if (ssid && pass && strlen(pass) >= 8) {
      prefs.begin("soccerbot", false);
      prefs.putString("ssid", ssid);
      prefs.putString("pass", pass);
      prefs.end();

      Serial.println("WiFi reconfigured, restarting...");
      delay(1000);
      ESP.restart();
    }
  }

  return cmd;
}

void executeCommand(Command cmd) {
  // TODO: Add motor control here
  // Example for omnidirectional drive:
  // setMotors(cmd.forward, cmd.sideways, cmd.rotation);

  Serial.printf("Execute: F=%.2f, S=%.2f, R=%.2f\n",
               cmd.forward, cmd.sideways, cmd.rotation);
}

void stopRobot() {
  // TODO: Stop all motors
  Serial.println("Robot stopped");
}

void updateLEDs() {
  // Status LED - solid when connected, blink when disconnected
  if (wifiConnected) {
    digitalWrite(LED_STATUS_PIN, HIGH);
  } else {
    digitalWrite(LED_STATUS_PIN, (millis() / 500) % 2);
  }

  // WiFi LED
  digitalWrite(LED_WIFI_PIN, wifiConnected);
}

void flashLED(int pin, int duration) {
  digitalWrite(pin, HIGH);
  delay(duration);
  digitalWrite(pin, LOW);
}
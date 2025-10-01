#ifndef MINIBOT_H
#define MINIBOT_H

#include <WiFi.h>
#include <WiFiUdp.h>

// WiFi and network constants
const char* const WIFI_SSID = "WATCHTOWER";
const char* const WIFI_PASSWORD = "lancerrobotics";
const unsigned int UDP_PORT = 2367;

class Minibot {
private:
  // Controller Values
  int leftX;
  int leftY;
  int rightX;
  int rightY;
  bool cross;
  bool circle;
  bool square;
  bool triangle;

  // Robot ID
  const char* robotId;

  // UDP
  WiFiUDP udp;
  char incomingPacket[255];
  String gameStatus;

  // Motor Pins
  int leftPin;
  int rightPin;
  int dcMotorPin;
  int servoMotorPin;

public:
  // Constructor
  Minibot(const char* robotId, 
          int leftMotorPin = 16, int rightMotorPin = 17,
          int dcMotorPin = 18, int servoMotorPin = 19);

  // Initialize the minibot (call this in setup())
  void init();

  // Update controller values from UDP (call this at the start of loop())
  void updateController();

  // Get controller input values
  int getLeftX();      // Returns 0-255 (center ~125)
  int getLeftY();      // Returns 0-255 (center ~130)
  int getRightX();     // Returns 0-255 (center ~127)
  int getRightY();     // Returns 0-255 (center ~130)

  // Get button states
  bool getCross();     // Returns true if pressed
  bool getCircle();    // Returns true if pressed
  bool getSquare();    // Returns true if pressed
  bool getTriangle();  // Returns true if pressed

  // Get game status
  String getGameStatus(); // Returns "standby", "teleop", etc.

  // Drive motors
  bool driveDCMotor(float value);     // value between -1 and 1
  bool drive(int pin, float value); // value between -1 and 1
  bool driveServoMotor(int angle);    // angle between -50 and 50
};

#endif
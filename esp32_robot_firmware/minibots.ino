#include "minibot.h"

// Create minibot object with robot ID and motor pins
// Preferred motor pins: left=16, right=17, dcMotor=18, servoMotor=19
Minibot bot("YOUR NAME HERE");

void setup() {
  Serial.begin(115200);
  delay(100);

  Serial.println("\n=== Minibot Starting ===");
  Serial.println("Initializing robot...");

  // Initialize the robot (WiFi, motors, UDP)
  bot.begin();

  Serial.println("Robot ready!");
}

void loop() {
  // Update controller values from the remote
  bot.updateController();

  // TODO: Write your robot control code here
  // Example:
  // float speed = (bot.getLeftY() - 127) / 127.0;
  // bot.driveLeftMotor(speed);
  // bot.driveRightMotor(speed);
}

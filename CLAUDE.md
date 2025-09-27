# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the SoccerBots Robotics Control System - a Java-based host application for controlling ESP32-based soccer robots with real-time controller input, WiFi networking, and Bluetooth configuration.

### Core Architecture

The system uses a **client-server UDP architecture**:
- **Host Application** (Java): Manages controllers, network, and robot communication
- **ESP32 Robots**: Receive UDP commands and execute movement/actions
- **Communication**: UDP broadcasts for discovery (port 12345), UDP unicast for commands (port 12346)
- **Configuration**: Bluetooth for initial WiFi setup on robots

### Key Components

- **`RoboticsControlApp.java`**: Main entry point, initializes Swing GUI
- **`gui/MainWindow.java`**: Primary UI orchestrating all panels
- **`network/NetworkManager.java`**: WiFi hosting, UDP communication, robot discovery
- **`robot/RobotManager.java`**: Robot discovery, pairing, command sending
- **`controller/ControllerManager.java`**: USB controller input processing via JInput
- **`robot/RobotCommand.java`**: JSON command structure sent to robots

### Data Flow

1. Controllers → ControllerManager → input processing → movement values
2. Movement values → RobotManager → JSON commands → UDP to robots
3. Robot discovery via UDP broadcast/response on network
4. Bluetooth configuration for initial robot WiFi setup

## Development Commands

### Building and Running
```bash
# Compile and package with dependencies
mvn clean compile assembly:single

# Run the application
java -jar target/robotics-control-system-1.0.0-jar-with-dependencies.jar

# Clean build
mvn clean

# Run tests (when test directory exists)
mvn test
```

### Debug Mode
Enable verbose logging:
```bash
java -Dlogging.level.com.soccerbots=DEBUG -jar target/robotics-control-system-1.0.0-jar-with-dependencies.jar
```

## Technology Stack

- **Java 11+** with Swing GUI
- **Maven** for build management
- **Jackson** for JSON processing
- **JInput** for game controller support
- **SLF4J + Logback** for logging
- **BlueZ Java** for Bluetooth communication

## Code Patterns

### Robot Communication
- Commands sent as JSON via UDP: `{"command": "move", "forward": 0.5, "sideways": 0.0, "rotation": 0.2}`
- Robot discovery uses broadcast/response pattern
- All network operations are asynchronous

### Controller Input
- JInput library polls USB controllers at ~60Hz
- Input mapped to normalized values (-1.0 to 1.0)
- Controller-to-robot pairing managed in ControllerPanel

### Error Handling
- Comprehensive logging throughout all components
- Network timeouts and reconnection logic
- Graceful degradation when controllers/robots disconnect

## ESP32 Integration

The `esp32_robot_code/` directory contains Arduino firmware that:
- Connects to WiFi networks configured via Bluetooth
- Receives UDP commands on port 12346
- Responds to discovery broadcasts on port 12345
- Uses JSON parsing for command interpretation

## Development Notes

- No existing test directory - tests would need to be created in `src/test/java/`
- Main class: `com.soccerbots.control.RoboticsControlApp`
- GUI uses Swing with system look-and-feel
- Network operations require proper firewall configuration
- Controller compatibility depends on DirectInput support
- Bluetooth functionality is planned but not yet implemented - dependency is commented out in pom.xml

## Build Issues Fixed

- **Bluetooth dependency**: Removed non-existent `org.bluez:bluez-java:0.3.0` dependency
- **UIManager method**: Fixed `getSystemLookAndFeel()` to `getSystemLookAndFeelClassName()`
- **Method naming conflict**: Renamed `updateUI()` to `updateUIElements()` in NetworkPanel to avoid Swing conflict
- **Controller button mapping**: Fixed `ordinal()` method issue by using hashCode-based button indexing
# SoccerBots Robotics Control System

A comprehensive, low-latency control system for ESP32-based soccer robots with Java GUI host application, WiFi networking, Bluetooth configuration, and real-time controller input.

![System Architecture](https://img.shields.io/badge/Platform-ESP32%20%2B%20Java-blue)
![License](https://img.shields.io/badge/License-MIT-green)
![Version](https://img.shields.io/badge/Version-1.0.0-orange)

## 🚀 Features

### Host Application (Java)
- **🖥️ Modern GUI** - Intuitive interface for managing robots and controllers
- **📡 WiFi Management** - Host your own network or connect to existing networks
- **🎮 Controller Support** - USB game controller input with automatic detection
- **🔍 Robot Discovery** - Automatic detection and pairing of robots
- **📊 Real-time Status** - Live monitoring of connections and system health
- **⚡ Low Latency** - Optimized for responsive robot control (16ms update rate)

### ESP32 Robot Firmware
- **📶 WiFi Connectivity** - Automatic connection with reconnection logic
- **📲 Bluetooth Configuration** - Configure WiFi settings via Bluetooth
- **📨 UDP Command Processing** - High-speed command receiving and parsing
- **💡 LED Status Indicators** - Visual feedback for system status
- **🔧 Persistent Configuration** - Settings saved in non-volatile storage
- **🔄 Auto-discovery** - Automatic network presence broadcasting

### Communication Protocol
- **🌐 UDP Networking** - Low-latency communication protocol
- **📋 JSON Commands** - Structured command format for robot control
- **🔐 Secure Configuration** - Bluetooth-based initial setup
- **📡 Broadcast Discovery** - Automatic robot detection system

## 📋 Requirements

### Host Computer
- **OS**: Windows 10/11, macOS 10.14+, or Linux Ubuntu 18.04+
- **Java**: Java 11 or later
- **RAM**: 4GB minimum, 8GB+ recommended
- **Network**: WiFi adapter with hosted network capability
- **Controllers**: USB game controllers (Xbox, PlayStation, etc.)

### ESP32 Robots
- **Hardware**: ESP32-WROOM development boards
- **Power**: 5V power supply or battery pack
- **LEDs**: 4x status LEDs with 220Ω resistors
- **Tools**: Arduino IDE 1.8.19+ with ESP32 board support

## 🛠️ Quick Start

### 1. Host Application
```bash
# Clone the repository
git clone <repository-url>
cd SoccerBotsSoftwareJava

# Build the application
mvn clean compile assembly:single

# Run the application
java -jar target/robotics-control-system-1.0.0-jar-with-dependencies.jar
```

### 2. ESP32 Setup
1. Install Arduino IDE and ESP32 board support
2. Install ArduinoJson library
3. Configure robot settings in `soccerbot_main.ino`:
   ```cpp
   const char* ROBOT_TEAM_NAME = "Your_Team_Name";
   const char* ROBOT_DEFAULT_NAME = "SoccerBot_01";
   const char* ROBOT_ID = "ROBOT_001";
   ```
4. Upload firmware to ESP32
5. Configure WiFi via Bluetooth or GUI

### 3. System Configuration
1. **Start Host Application** and configure network
2. **Discover Robots** using the discovery feature
3. **Connect Controllers** via USB
4. **Pair Controllers** with discovered robots
5. **Start Controlling** robots with controller inputs

## 📁 Project Structure

```
SoccerBotsSoftwareJava/
├── src/main/java/com/soccerbots/control/
│   ├── RoboticsControlApp.java          # Main application entry point
│   ├── gui/                             # User interface components
│   │   ├── MainWindow.java              # Main application window
│   │   ├── NetworkPanel.java            # Network configuration
│   │   ├── RobotPanel.java              # Robot management
│   │   ├── ControllerPanel.java         # Controller management
│   │   └── StatusPanel.java             # System status display
│   ├── network/                         # Network management
│   │   └── NetworkManager.java          # WiFi and UDP handling
│   ├── robot/                           # Robot communication
│   │   ├── RobotManager.java            # Robot discovery and control
│   │   ├── Robot.java                   # Robot data model
│   │   └── RobotCommand.java            # Command structure
│   └── controller/                      # Controller input
│       ├── ControllerManager.java       # Input handling and processing
│       ├── GameController.java          # Controller abstraction
│       └── ControllerInput.java         # Input data model
├── esp32_robot_code/
│   ├── soccerbot_main/
│   │   └── soccerbot_main.ino          # Main ESP32 firmware
│   └── libraries_required.txt          # Required Arduino libraries
├── pom.xml                              # Maven build configuration
├── INSTALLATION_GUIDE.md               # Detailed setup instructions
├── USER_MANUAL.md                      # Complete user documentation
└── README.md                           # This file
```

## 🔧 Hardware Setup

### ESP32 Connections
```
GPIO Pin  | Function              | Connection
----------|----------------------|----------------------------------
GPIO 2    | System Status LED    | Built-in LED
GPIO 4    | WiFi Status LED      | LED + 220Ω resistor → GND
GPIO 5    | Bluetooth Status LED | LED + 220Ω resistor → GND
GPIO 18   | Command Status LED   | LED + 220Ω resistor → GND
3.3V/VIN  | Power               | 3.3V or 5V power supply
GND       | Ground              | Power supply ground
```

### Recommended Controllers
- Xbox One/Series Controllers (USB)
- PlayStation 4/5 Controllers (USB)
- Generic USB gamepads with analog sticks
- Any DirectInput compatible controller

## 🌐 Network Architecture

### Communication Protocols
- **Discovery**: UDP broadcast on port 12345
- **Commands**: UDP unicast on port 12346
- **Configuration**: Bluetooth Serial (initial setup)

### Data Flow
1. **Host discovers robots** via UDP broadcast
2. **Robots respond** with identification data
3. **Controllers send input** to host application
4. **Host translates** input to robot commands
5. **Commands sent** via UDP to paired robots
6. **Robots execute** movement commands

## 📚 Documentation

### Complete Guides
- **[Installation Guide](INSTALLATION_GUIDE.md)** - Step-by-step setup instructions
- **[User Manual](USER_MANUAL.md)** - Complete operational documentation

### Key Topics
- System requirements and compatibility
- Hardware assembly and connections
- Software installation and configuration
- Network setup and optimization
- Robot discovery and pairing
- Controller configuration and mapping
- Troubleshooting common issues
- Performance optimization tips

## 🎮 Usage Examples

### Basic Robot Control
```java
// Discover robots on the network
robotManager.startDiscovery();

// Send movement command to robot
robotManager.sendMovementCommand("ROBOT_001", 0.5, 0.0, 0.2);
// Parameters: robotId, forward, sideways, rotation (-1.0 to 1.0)

// Stop robot
robotManager.sendStopCommand("ROBOT_001");
```

### Bluetooth Configuration
```json
// Configure robot WiFi via Bluetooth
{
  "command": "configure_wifi",
  "ssid": "Your_Network_Name",
  "password": "your_password"
}

// Get robot status
{
  "command": "get_status"
}

// Set robot name
{
  "command": "set_name",
  "name": "NewRobotName"
}
```

## 🔧 Development

### Building from Source
```bash
# Compile and package
mvn clean compile assembly:single

# Run tests
mvn test

# Clean build
mvn clean
```

### Adding New Features
1. **Robot Commands**: Extend `RobotCommand` class and update ESP32 parser
2. **Controller Mappings**: Modify `ControllerInput` processing
3. **Network Protocols**: Update `NetworkManager` for new communication methods
4. **GUI Components**: Add new panels to `MainWindow` layout

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add comprehensive javadoc comments
- Maintain consistent indentation (4 spaces)
- Keep methods focused and concise

## 🐛 Troubleshooting

### Common Issues
| Issue | Cause | Solution |
|-------|-------|----------|
| No robots discovered | Network/firewall | Check network connection, disable firewall |
| Controller not detected | Drivers/compatibility | Install drivers, try different controller |
| High latency | Network congestion | Use dedicated network, reduce interference |
| Robot disconnects | Power/interference | Check power supply, reduce WiFi interference |

### Debug Mode
Enable verbose logging by adding JVM argument:
```bash
java -Dlogging.level.com.soccerbots=DEBUG -jar application.jar
```

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

### Code Guidelines
- Follow existing code style and patterns
- Add unit tests for new features
- Update documentation for API changes
- Test on multiple platforms when possible

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **ESP32 Community** for excellent documentation and examples
- **Arduino Project** for the development environment
- **JInput Library** for controller support
- **Jackson JSON** for efficient JSON processing

## 📞 Support

### Getting Help
- **Documentation**: Check Installation Guide and User Manual
- **Issues**: Report bugs on the project repository
- **Community**: Join the SoccerBots development community
- **Email**: Contact the development team for support

### Version History
- **v1.0.0** - Initial release with core functionality
  - WiFi network management
  - Robot discovery and control
  - Controller input processing
  - Bluetooth configuration
  - Complete documentation

---

**Made with ❤️ for the robotics community**
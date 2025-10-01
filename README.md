# SoccerBots Control Station

A modern, Grok AI-inspired JavaFX control system for ESP32-based soccer robots with real-time controller input, WiFi networking, and professional dark-mode interface.

![System Architecture](https://img.shields.io/badge/Platform-ESP32%20%2B%20JavaFX-blue)
![License](https://img.shields.io/badge/License-MIT-green)
![Version](https://img.shields.io/badge/Version-1.0.0-orange)
![Theme](https://img.shields.io/badge/UI-Grok%20Inspired-purple)

## 🚀 Features

### Modern JavaFX Interface
- **🎨 Grok AI-Inspired Theme** - Dark-mode interface with cosmic aesthetics
- **💫 Smooth Animations** - 150-200ms transitions with subtle scaling effects
- **🎯 Intuitive Navigation** - Clean header with pill-shaped navigation buttons
- **📱 Responsive Design** - Adapts to different screen sizes and resolutions
- **♿ High Accessibility** - Strong contrast ratios and readable typography
- **🌙 Dark-First Design** - Professional appearance optimized for control rooms

### ESP32 Robot Integration
- **📡 WATCHTOWER Network** - Dedicated network for ESP32 robot communication
- **🔗 Manual Robot Addition** - Direct IP-based robot connections
- **⚡ Binary UDP Protocol** - High-performance 24-byte command packets
- **🎮 Direct Controller Mapping** - Real-time stick-to-robot input translation
- **🟢 Game State Management** - Teleop/standby mode control
- **📊 Live Status Monitoring** - Real-time connection and robot status

### Communication Protocol
- **🌐 ESP32 UDP** - Port 2367 binary communication
- **📋 Binary Commands** - Optimized packet structure (16 + 6 + 2 bytes)
- **🎯 Robot Targeting** - Name-based robot identification
- **🔄 Real-time Updates** - ~60Hz controller input processing

## 📋 Requirements

### Host Computer
- **OS**: Windows 10/11, macOS 10.14+, or Linux Ubuntu 18.04+
- **Java**: Java 17 or later (with JavaFX support)
- **RAM**: 4GB minimum, 8GB+ recommended
- **Network**: WiFi connection to WATCHTOWER network
- **Controllers**: USB game controllers (Xbox, PlayStation, etc.)

### ESP32 Robots
- **Hardware**: ESP32-WROOM development boards
- **Network**: Pre-configured for WATCHTOWER WiFi network
- **Power**: 5V power supply or battery pack
- **Communication**: UDP port 2367 listener

## 🛠️ Quick Start

### 1. Host Application Setup
```bash
# Clone the repository
git clone <repository-url>
cd SoccerBotsSoftwareJava

# Build the JavaFX application
mvn clean compile package

# Run the Grok-themed control station
java -jar target/robotics-control-system-1.0.0-jar-with-dependencies.jar
```

### 2. ESP32 Robot Configuration
1. **Flash ESP32 firmware** with WATCHTOWER network credentials
2. **Power on robots** - they will auto-connect to WATCHTOWER network
3. **Note robot IP addresses** for manual addition to control station

### 3. System Operation
1. **Connect to WATCHTOWER Network** on your host computer
2. **Launch Control Station** - enjoy the Grok-inspired interface
3. **Add Robots Manually** using name and IP address
4. **Connect USB Controllers** - they will be auto-detected
5. **Pair Controllers** with robots using the interface
6. **Enable Teleop Mode** to start robot control

## 🎨 Grok-Inspired Design

### Color Palette
- **Primary Background**: Deep Black (#0A0A0A) for cosmic feel
- **Surface Colors**: Soft Grays (#1A1A1A, #2A2F36) for cards
- **Text**: High-contrast White/Off-white (#FFFFFF, #EDEDED)
- **Accents**: Blue (#1D9BF0), Green (#22C55E), Yellow (#EAB308), Red (#EF4444)

### Typography
- **Font Family**: Inter, SF Pro Display, Roboto, system fonts
- **Sizes**: 14px body, 16px labels, 24px titles
- **Weight**: 400 regular, 500 medium, 600 semi-bold
- **Letter Spacing**: Slight negative (-0.06em) for modern look

### UI Components
- **Pill-Shaped Buttons**: Rounded with hover scaling (1.02x)
- **Card-Based Layout**: Subtle shadows and clean borders
- **Status Indicators**: Color-coded circles with drop shadows
- **Smooth Animations**: Fade and scale transitions throughout

## 📁 Project Structure

```
SoccerBotsSoftwareJava/
├── src/main/java/com/soccerbots/control/
│   ├── RoboticsControlFXApp.java        # JavaFX application entry point
│   ├── gui/                             # Grok-themed UI components
│   │   ├── MainWindow.java              # Main window with header navigation
│   │   ├── RobotPanel.java              # ESP32 robot management
│   │   ├── ControllerPanel.java         # USB controller management
│   │   ├── NetworkPanel.java            # WATCHTOWER network status
│   │   ├── MonitoringPanel.java         # System monitoring
│   │   └── SettingsPanel.java           # Application settings
│   ├── network/                         # ESP32 network communication
│   │   └── NetworkManager.java          # UDP binary protocol handler
│   ├── robot/                           # ESP32 robot management
│   │   ├── RobotManager.java            # Robot discovery and control
│   │   ├── Robot.java                   # Robot data model
│   │   └── ESP32Command.java            # Binary command structure
│   └── controller/                      # Controller input processing
│       ├── ControllerManager.java       # USB controller handling
│       ├── GameController.java          # Controller abstraction
│       └── ControllerInput.java         # Input normalization
├── src/main/resources/
│   └── styles/
│       └── grok.css                     # Grok AI-inspired theme
├── esp32_robot_code/                    # ESP32 firmware (provided)
├── docs/                                # Project documentation
├── pom.xml                              # Maven build with JavaFX
└── README.md                            # This file
```

## 🔧 ESP32 Communication Protocol

### Binary Packet Structure (24 bytes)
```
Bytes 0-15:  Robot name (null-padded string)
Bytes 16-19: Stick axes (leftX, leftY, rightX, rightY) [0-255]
Bytes 20-21: Unused axes (reserved)
Bytes 22:    Button data (cross, circle, square, triangle)
Bytes 23:    Unused buttons (reserved)
```

### Controller to ESP32 Mapping
- **Left Stick**: Forward/backward and sideways movement
- **Right Stick**: Rotation and unused axis
- **Buttons**: PlayStation-style (Cross, Circle, Square, Triangle)
- **Values**: Normalized to 0-255 range with center points

## 🎮 Interface Guide

### Main Navigation
- **Robots** - Add and manage ESP32 robots
- **Controllers** - View and pair USB controllers
- **Network** - Monitor WATCHTOWER network status
- **Monitoring** - System performance and status
- **Settings** - Application configuration

### Robot Management
1. **Network Status** - Shows WATCHTOWER connection
2. **Add Robot** - Manual entry with name and IP
3. **Robot Cards** - Live status with action buttons
4. **Teleop Control** - Enable/disable robot movement

### Controller Operations
- **Auto-Detection** - USB controllers appear automatically
- **Pairing** - Assign controllers to specific robots
- **Real-time Input** - Live stick and button monitoring
- **Emergency Stop** - Immediate halt for all robots

## 🌐 Network Architecture

### WATCHTOWER Network
- **Purpose**: Dedicated ESP32 robot communication
- **Protocol**: UDP port 2367 for binary commands
- **Discovery**: Manual robot addition by IP address
- **Security**: Network-level access control

### Data Flow
1. **Controller Input** → ControllerManager (60Hz polling)
2. **Input Processing** → ESP32Command binary conversion
3. **Network Transmission** → UDP to robot IP:2367
4. **Robot Execution** → Movement command processing

## 📚 Documentation

### Complete Guides
- **[Project Structure](PROJECT_STRUCTURE.md)** - Detailed file explanations
- **[User Manual](USER_MANUAL.md)** - Complete operational guide
- **[Installation Guide](INSTALLATION_GUIDE.md)** - Setup instructions

### Key Topics
- Grok theme customization and CSS variables
- ESP32 binary protocol implementation
- JavaFX application architecture
- Controller input processing and mapping
- Network configuration and troubleshooting

## 🎮 Usage Examples

### Basic Robot Control
```java
// Add robot manually
Robot robot = robotManager.addRobot("MyRobot", "192.168.1.100");

// Send controller input (normalized -1.0 to 1.0)
robotManager.sendMovementCommand(
    "MyRobot",
    leftStickX, leftStickY,
    rightStickX, rightStickY
);

// Enable teleop mode
robotManager.startTeleop();

// Emergency stop all robots
robotManager.emergencyStopAll();
```

### ESP32 Binary Command
```java
// Create command for ESP32
ESP32Command cmd = ESP32Command.fromControllerInput(
    "MyRobot",
    0.5,  // leftStickX
    -0.3, // leftStickY
    0.0,  // rightStickX
    0.8,  // rightStickY
    true, false, false, false // buttons
);

// Send via UDP to robot
networkManager.sendRobotCommand(robotName, robotIP,
    cmd.getLeftX(), cmd.getLeftY(),
    cmd.getRightX(), cmd.getRightY(),
    cmd.isCross(), cmd.isCircle(),
    cmd.isSquare(), cmd.isTriangle());
```

## 🔧 Development

### Building from Source
```bash
# Compile with JavaFX dependencies
mvn clean compile package

# Run with debug logging
java -Dlogging.level.com.soccerbots=DEBUG -jar target/robotics-control-system-1.0.0-jar-with-dependencies.jar

# Clean build
mvn clean
```

### Customizing Grok Theme
Edit `src/main/resources/styles/grok.css`:
```css
.root {
    /* Customize colors */
    -primary-bg: #0A0A0A;
    -accent-blue: #1D9BF0;
    -success-green: #22C55E;

    /* Adjust fonts */
    -font-family: "Your-Font", system-ui;
    -font-size-body: 14px;
}
```

### Adding New Features
1. **UI Components**: Use Grok CSS classes (.grok-button, .grok-card, etc.)
2. **Robot Commands**: Extend ESP32Command for new functionality
3. **Controller Mappings**: Modify ControllerInput processing
4. **Network Protocols**: Update NetworkManager for new communication

## 🐛 Troubleshooting

### Common Issues
| Issue | Cause | Solution |
|-------|-------|----------|
| Interface appears broken | Missing CSS | Check grok.css is in classpath |
| No robots found | Network connection | Verify WATCHTOWER network connection |
| Controller not working | Driver issues | Install controller drivers, check USB |
| Robot not responding | IP/Network | Verify robot IP and WATCHTOWER connection |
| Animation stuttering | Performance | Reduce animation complexity or disable |

### Network Diagnostics
```bash
# Test UDP communication
ping 192.168.1.100

# Check network interface
ipconfig /all

# Verify WATCHTOWER connection
netsh wlan show interfaces
```

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Ensure Java 17+ with JavaFX support
3. Import as Maven project
4. Run `mvn clean compile` to verify setup
5. Make changes and test with Grok theme

### Code Guidelines
- Follow existing Grok theme patterns
- Use CSS variables for consistent styling
- Maintain 150-200ms animation timing
- Test with different screen sizes
- Update documentation for UI changes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Grok AI** for design inspiration and aesthetic guidelines
- **JavaFX Community** for modern UI framework support
- **ESP32 Community** for excellent documentation and examples
- **JInput Library** for controller support
- **Inter Font Family** for beautiful typography

## 📞 Support

### Getting Help
- **Documentation**: Check User Manual and Project Structure guide
- **Issues**: Report bugs with screenshots of Grok interface
- **Community**: Join the SoccerBots development community
- **Theme Issues**: Include CSS and JavaFX version details

### Version History
- **v1.0.0** - Grok-themed JavaFX release
  - Modern dark-mode interface with Grok AI aesthetics
  - ESP32-specific binary communication protocol
  - Manual robot addition and management
  - Real-time controller input with smooth animations
  - Professional typography and responsive design

---

**Built with modern JavaFX and inspired by Grok AI design principles ✨**
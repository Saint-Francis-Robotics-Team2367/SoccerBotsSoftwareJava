# SoccerBots Control Station

A modern robot control system for ESP32-based soccer robots with multiple interface options: a sleek Electron-based React UI, JavaFX GUI, and a lightweight 3D simulator.

![System Architecture](https://img.shields.io/badge/Platform-ESP32%20%2B%20React%20%2B%20Java-blue)
![License](https://img.shields.io/badge/License-MIT-green)
![Version](https://img.shields.io/badge/Version-1.0.0-orange)

## 🚀 Overview

This project provides a complete robot control solution with:
- **Native Desktop App** - Modern React UI powered by Electron + Java backend
- **JavaFX GUI** - Classic desktop interface (legacy)
- **3D Simulator** - Lightweight robot simulator with controller support
- **Headless API Mode** - REST API + WebSocket server for custom frontends

## 📋 Requirements

### All Modes
- **Java**: JDK 17 or later
- **Maven**: 3.8+ for building
- **Controllers**: USB game controllers (Xbox, PlayStation, etc.) via JInput

### Native Desktop App (Electron + React)
- **Node.js**: 18+ and npm
- **OS**: Windows 10/11, macOS 10.14+, or Linux

### ESP32 Robots
- **Hardware**: ESP32-WROOM development boards
- **Network**: WiFi connection to same network as host
- **Communication**: UDP ports 12345-12346

## 🛠️ Quick Start

### Option 1: Native Desktop App (Recommended)

The modern Electron + React interface with real-time updates.

```bash
# 1. Install all dependencies
npm run install:all

# 2. Build Java backend
npm run build:backend

# 3. Build frontend
npm run build:frontend

# 4. Run the app
npm start
```

**Development Mode** (3 terminals for hot reload):
```bash
# Terminal 1: Java backend with API
npm run dev:backend

# Terminal 2: React frontend (Vite dev server)
npm run dev:frontend

# Terminal 3: Electron window
npm run dev:electron
```

**Build Distributable:**
```bash
npm run dist
# Creates installers in electron/dist/
```

### Option 2: JavaFX GUI (Legacy)

Traditional desktop interface using Java Swing/JavaFX.

```bash
# Build and run
mvn clean compile
mvn javafx:run
```

### Option 3: 3D Simulator

Lightweight robot simulator for testing without hardware.

```bash
# Compile
mvn clean compile assembly:single

# Run simulator
java -cp target/robotics-control-system-1.0.0-jar-with-dependencies.jar \
  com.soccerbots.control.simulator.SimulatorApp
```

### Option 4: Headless API Mode

Run backend with HTTP REST API + WebSocket for custom frontends.

```bash
# Build backend
mvn clean compile assembly:single

# Run headless (API on port 8080)
java -cp target/robotics-control-system-1.0.0-jar-with-dependencies.jar \
  com.soccerbots.control.HeadlessLauncher
```

## 🎨 Native Desktop App Features

### Modern React UI
- **Robot Connection Panel** - Manage multiple robots, view status and signal strength
- **Network Analysis** - Real-time latency and bandwidth charts
- **Control Panel** - Emergency stop, system controls
- **Terminal Monitor** - Live command output and system logs
- **Service Log** - Event tracking with timestamps and severity levels

### Backend API
- **REST API** - Full robot/controller/network management
- **WebSocket** - Real-time updates and event streaming
- **Auto-Discovery** - Finds robots on network via UDP broadcast
- **Controller Support** - Automatic USB controller detection via JInput

### Real-Time Features
- Live robot status updates
- Network performance monitoring
- Instant emergency stop
- Controller input visualization
- System event logging

## 📁 Project Structure

```
SoccerBotsSoftwareJava/
├── src/main/java/com/soccerbots/control/
│   ├── api/                          # HTTP REST API & WebSocket
│   │   └── ApiServer.java            # Javalin server with endpoints
│   ├── controller/                   # Game controller input
│   │   ├── ControllerManager.java    # USB controller handling
│   │   ├── GameController.java       # Controller abstraction
│   │   └── ControllerInput.java      # Input normalization
│   ├── network/                      # WiFi and UDP networking
│   │   └── NetworkManager.java       # WiFi hosting, UDP comm
│   ├── robot/                        # Robot management
│   │   ├── RobotManager.java         # Discovery, pairing, commands
│   │   ├── Robot.java                # Robot data model
│   │   └── RobotCommand.java         # JSON command structure
│   ├── gui/                          # JavaFX GUI (legacy)
│   │   ├── MainWindow.java           # Main window
│   │   ├── RobotPanel.java           # Robot management
│   │   ├── ControllerPanel.java      # Controller pairing
│   │   └── NetworkPanel.java         # Network status
│   ├── simulator/                    # 3D Robot Simulator
│   │   ├── SimulatorApp.java         # Main simulator window
│   │   ├── SimulatorRenderer.java    # 3D rendering
│   │   ├── SimulatorWorld.java       # Physics world
│   │   └── SimulatedRobot.java       # Robot physics
│   ├── HeadlessLauncher.java         # API mode entry point
│   ├── Launcher.java                 # JavaFX GUI launcher
│   └── RoboticsControlApp.java       # Swing GUI launcher
├── frontend/                         # React + TypeScript UI
│   ├── src/
│   │   ├── components/               # React components
│   │   │   ├── ConnectionPanel.tsx   # Robot connections
│   │   │   ├── NetworkAnalysis.tsx   # Charts
│   │   │   ├── ControlPanel.tsx      # Emergency stop
│   │   │   ├── ServiceLog.tsx        # Event log
│   │   │   └── TerminalMonitor.tsx   # Terminal output
│   │   ├── services/
│   │   │   └── api.ts                # Backend API client
│   │   ├── App.tsx                   # Main app component
│   │   └── main.tsx                  # Entry point
│   ├── package.json
│   └── vite.config.ts
├── electron/                         # Electron wrapper
│   ├── main.js                       # Main process (launches Java)
│   ├── preload.js                    # Context bridge
│   └── package.json
├── esp32_robot_firmware/             # ESP32 Arduino firmware
├── package.json                      # Root build scripts
├── pom.xml                           # Maven configuration
├── README.md                         # This file
└── ELECTRON_APP_README.md            # Detailed Electron app guide
```

## 🔌 Backend API Reference

### REST Endpoints (Port 8080)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| GET | `/api/robots` | List all robots |
| GET | `/api/robots/{id}` | Get robot details |
| POST | `/api/robots/{id}/connect` | Connect to robot |
| POST | `/api/robots/{id}/disconnect` | Disconnect robot |
| POST | `/api/robots/{id}/enable` | Enable robot |
| POST | `/api/robots/{id}/disable` | Disable robot |
| POST | `/api/robots/refresh` | Scan for robots |
| GET | `/api/controllers` | List controllers |
| POST | `/api/emergency-stop` | Activate emergency stop |
| POST | `/api/emergency-stop/deactivate` | Deactivate emergency stop |
| GET | `/api/network/stats` | Network statistics |

### WebSocket (ws://localhost:8080/ws)

Real-time events:
- `robot_connected` - Robot connected
- `robot_disconnected` - Robot disconnected
- `emergency_stop` - Emergency stop state changed
- `robot_enabled` / `robot_disabled` - Robot state changed

## 🌐 Robot Communication Protocol

### Discovery Protocol (UDP Port 12345)
- **Broadcast**: Host sends discovery requests
- **Response**: Robots reply with ID and IP address

### Command Protocol (UDP Port 12346)
- **Format**: JSON with movement commands
- **Example**:
  ```json
  {
    "command": "move",
    "forward": 0.5,
    "sideways": 0.0,
    "rotation": 0.2
  }
  ```

### Controller Mapping
- **Left Stick**: Forward/sideways movement
- **Right Stick X**: Rotation
- **Values**: Normalized -1.0 to 1.0

## 🎮 Using the Native Desktop App

### 1. Launch Application
```bash
npm start
```

### 2. Scan for Robots
- Click **Refresh** in the Robot Connections panel
- Robots on your network will appear automatically

### 3. Connect to Robots
- Click **Connect** on any discovered robot
- Status indicator will turn green when connected

### 4. Attach Controllers
- Plug in USB game controller
- Controllers appear automatically in the interface
- Input is sent to paired robots in real-time

### 5. Emergency Stop
- Red **Emergency Stop** button in Control Panel
- Instantly halts all robot operations
- Click again to resume

### 6. Monitor System
- **Network Analysis**: View latency and bandwidth charts
- **Terminal Monitor**: See real-time system commands
- **Service Log**: Track all events with timestamps

## 🔧 Development

### Project Setup
```bash
# Install frontend & electron dependencies
npm run install:all

# Build Java backend
mvn clean compile assembly:single
```

### Running in Development

**Electron App (Hot Reload):**
```bash
# Terminal 1: Backend API
npm run dev:backend

# Terminal 2: Frontend (Vite)
npm run dev:frontend

# Terminal 3: Electron
npm run dev:electron
```

**JavaFX GUI:**
```bash
mvn javafx:run
```

**3D Simulator:**
```bash
mvn exec:java -Dexec.mainClass="com.soccerbots.control.simulator.SimulatorApp"
```

### Building for Distribution

**Electron App:**
```bash
npm run dist
# Output: electron/dist/ (platform-specific installers)
```

**Standalone JAR:**
```bash
mvn clean compile assembly:single
# Output: target/robotics-control-system-1.0.0-jar-with-dependencies.jar
```

## 🐛 Troubleshooting

### Native Desktop App

**Backend won't start:**
- Ensure Java 17+ installed: `java -version`
- Check port 8080 is available
- View logs in Electron DevTools console

**Frontend won't load:**
- Check `frontend/dist/` exists after build
- In dev mode, verify Vite server on port 5173
- Open DevTools to see errors (Ctrl+Shift+I)

**WebSocket connection failed:**
- Backend must start before frontend
- Check backend console for "API server running"
- Verify `frontend/src/services/api.ts` API URL

### Controllers Not Detected

- Install JInput native libraries (automatic with Maven)
- Windows: Ensure DirectInput drivers installed
- Try refreshing controller list in app
- Check USB connection and permissions

### Robots Not Discovered

- Ensure robots and host on same WiFi network
- Check firewall allows UDP ports 12345-12346
- Verify ESP32 firmware running and configured
- Try manual refresh in UI

### Build Issues

**Maven errors:**
```bash
# Clean and rebuild
mvn clean
mvn compile
```

**npm errors:**
```bash
# Clear cache and reinstall
cd frontend && rm -rf node_modules package-lock.json && npm install
cd ../electron && rm -rf node_modules package-lock.json && npm install
```

## 📚 Additional Documentation

- **[ELECTRON_APP_README.md](ELECTRON_APP_README.md)** - Detailed Electron app guide
- **[INSTALLATION_GUIDE.md](INSTALLATION_GUIDE.md)** - Setup instructions
- **[USER_MANUAL.md](USER_MANUAL.md)** - Complete operational guide
- **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** - File-by-file explanations

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- **Figma Community** - RobotControlDesktopApp design template
- **React & Electron** - Modern desktop framework
- **JavaFX & Swing** - Classic Java UI frameworks
- **JInput Library** - Game controller support
- **Jackson & Javalin** - JSON processing and web framework
- **ESP32 Community** - Excellent hardware documentation

## 📞 Support

### Quick Reference

| Task | Command |
|------|---------|
| Run native app | `npm start` |
| Run JavaFX GUI | `mvn javafx:run` |
| Run simulator | `java -cp target/*-jar-with-dependencies.jar com.soccerbots.control.simulator.SimulatorApp` |
| Build everything | `npm run build:all` |
| Development mode | See "Running in Development" section |

---

**Choose your interface: Modern React UI, Classic JavaFX, or Lightweight Simulator** ✨

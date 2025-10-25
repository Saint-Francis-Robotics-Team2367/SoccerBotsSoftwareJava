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
- **Network**: WiFi connection to same network as host (default: "WATCHTOWER")
- **Communication**: UDP ports 12345 (discovery) and 2367 (commands - static)
- **Firmware**: Arduino IDE with ESP32 board support

## 🛠️ Quick Start

> **⚡ TL;DR - One Command to Run Everything:**
> ```bash
> npm run dev
> ```
> This starts the backend, frontend, and opens the native desktop app automatically!
> See [QUICKSTART.md](QUICKSTART.md) for details.

### ESP32 Robot Setup

**1. Install Arduino IDE and ESP32 Support**
```bash
# In Arduino IDE:
# - File → Preferences → Additional Board URLs
# - Add: https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
# - Tools → Board → Boards Manager → Search "ESP32" → Install
```

**2. Configure Robot Firmware**
```cpp
// In esp32_robot_firmware/minibots.ino
Minibot bot("YOUR_ROBOT_NAME_HERE");  // Change to unique name
```

**3. Upload to ESP32**
- Open `esp32_robot_firmware/minibots.ino` in Arduino IDE
- Select Board: "ESP32 Dev Module"
- Select Port: Your ESP32's COM port
- Click Upload

**4. Power On Robot**
- Robot connects to WiFi network "WATCHTOWER" (password: "lancerrobotics")
- Listens on static port 2367 for commands
- Sends discovery pings every 2 seconds
- Driver station automatically detects robot and adds to available list

**Robot Serial Monitor Output:**
- "Connecting to WiFi..." - Attempting WiFi connection
- "Connected! IP: X.X.X.X" - WiFi connected successfully
- "Listening on command port 2367" - Ready to receive commands
- "Sent discovery ping" - Broadcasting presence to driver station
- "EMERGENCY STOP ACTIVATED" - All motors stopped
- "Emergency stop released" - Ready for movement commands
- "Connection timeout - stopping motors" - No commands received for 5 seconds

**Important:** Each robot must have a unique name in the firmware!

### Option 1: Native Desktop App (Recommended) ⚡

The modern Electron + React interface with real-time updates.

**One Command Development:**
```bash
# Install dependencies (first time only)
npm run install:all

# Run everything with one command!
npm run dev
```
This automatically:
- ✅ Starts Java backend (port 8080)
- ✅ Starts React dev server (port 5173)
- ✅ Opens native Electron window
- ✅ Hot reloads on code changes

**Production Build:**
```bash
# Build everything and run
npm start

# Or create installer
npm run dist
```

**Manual Development** (if you need separate terminals):
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
**Simple Discovery System:**

1. **Robot Startup**: ESP32 powers on and connects to WiFi
2. **Discovery Ping**: Robot broadcasts `DISCOVER:<name>:<IP>` every 2 seconds
3. **Discovery**: Driver station detects robot and adds to available list
4. **Ready**: Robot listens on static port 2367 for commands
5. **Timeout**: After 5 seconds without commands, robot stops motors (but continues discovery)

**Benefits:**
- No manual IP configuration required
- Automatic reconnection after power cycle
- Minimal ESP32 code (~150 lines)
- Simple static port - no dynamic assignment needed

### Command Protocol (Static Port 2367)
**Binary Movement Commands (24 bytes):**
```
Bytes 0-15:  Robot name (null-padded)
Bytes 16-19: Axes (leftX, leftY, rightX, rightY) [0-255]
Bytes 20-21: Unused
Bytes 22:    Buttons (cross, circle, square, triangle)
Bytes 23:    Unused
```

**Text Commands:**
- `<name>:teleop` - Enable movement
- `<name>:standby` - Disable movement
- `ESTOP` - Emergency stop (stops all motors)
- `ESTOP_OFF` - Release emergency stop

### Emergency Stop Behavior
- Sent to **all robots** on discovery port (always monitored)
- Persists until `ESTOP_OFF` received or robot power cycled
- Blocks all movement commands while active
- Works even when robot is disconnected

### Controller Mapping
- **Left Stick**: Forward/sideways movement
- **Right Stick X**: Rotation
- **Values**: Normalized -1.0 to 1.0 (converted to 0-255 for ESP32)

**See [ROBOT_PROTOCOL.md](ROBOT_PROTOCOL.md) for complete protocol specification.**

## 🎮 Using the Native Desktop App

### 1. Launch Application
```bash
npm start
```

### 2. Automatic Robot Discovery
- **No manual scanning needed!** Robots automatically appear when powered on
- Driver station listens for discovery pings (passive mode)
- All robots use the same static port (2367) for commands
- Watch the **Robot Connections** panel for discovered robots

### 3. Connect to Robots
- Discovered robots show in the left panel with status "discovered"
- Click **Connect** to start sending commands
- Status changes to "connected" with green indicator
- Robot receives commands on static UDP port 2367

### 4. Attach Controllers
- Plug in USB game controller
- Controllers appear automatically in the interface
- Pair controller to robot in Controller panel
- Input is sent to paired robots in real-time

### 5. Emergency Stop
- Red **Emergency Stop** button in Control Panel
- Sends ESTOP command to **all robots** immediately
- All motors halt, movement blocked
- Click again to send ESTOP_OFF and resume operations
- **Note**: Emergency stop persists across disconnects and requires explicit release

### 6. Monitor System
- **Network Analysis**: View latency and bandwidth charts
- **Terminal Monitor**: See real-time system commands and discovery events
- **Service Log**: Track all events including robot discovery, connections, and emergency stops

### 7. Reconnection Handling
- If robot loses connection (timeout), motors stop but discovery continues
- Robot remains on same static port (2367)
- Driver station automatically re-detects robot via discovery pings
- No manual intervention required
- Power cycling robot clears emergency stop state

## 🔧 Development

### Project Setup
```bash
# Install all dependencies (first time only)
npm run install:all
```

### Running in Development

**⚡ Single Command (Recommended):**
```bash
npm run dev
```
Starts backend + frontend + Electron in one command! Opens native window automatically.

**Manual Control (if needed):**
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

**Check ESP32:**
- Open Arduino Serial Monitor (115200 baud)
- Look for "Connected! IP: X.X.X.X" - confirms WiFi connection
- Look for "Sent discovery ping" - confirms broadcasting
- Verify robot name matches what you expect

**Check Driver Station:**
- Ensure listening on port 12345 (check logs for "Discovery service started")
- Check firewall allows UDP port 12345 (incoming)
- Verify both robot and host are on same subnet (e.g., 192.168.1.x)
- Look in Terminal Monitor for discovery ping messages

**Common Issues:**
- WiFi credentials incorrect in firmware (WIFI_SSID/WIFI_PASSWORD)
- Firewall blocking UDP port 12345
- Robot and driver station on different networks
- Multiple driver stations responding (port conflict)

### Robot Won't Move

**Check Connection:**
- Robot must show "connected" status (green indicator)
- Check Serial Monitor for "Listening on command port 2367"
- Verify no emergency stop active (shows in UI and Serial Monitor)

**Check Game State:**
- Robot only moves in "teleop" mode
- Driver station must send game state command
- Check Terminal Monitor for game state messages

**Emergency Stop Active:**
- Look for "EMERGENCY STOP ACTIVATED" in Serial Monitor
- Red indicator in Control Panel
- Click emergency stop button to release
- Or power cycle robot to clear

### Robot Keeps Disconnecting

**Timeout Issues:**
- Robot expects commands every 5 seconds
- Check network latency and packet loss
- Verify driver station is sending keepalive messages
- Look for "Connection timeout" in Serial Monitor

**WiFi Issues:**
- Weak signal strength
- Network congestion
- Router interference
- Try moving robot closer to access point

### Emergency Stop Won't Release

- Click emergency stop button in Control Panel (shows ESTOP_OFF in logs)
- Verify command sent to robot IP (check Terminal Monitor)
- Robot Serial Monitor should show "Emergency stop released"
- If stuck: Power cycle robot (clears emergency stop state)

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

- **[ROBOT_PROTOCOL.md](ROBOT_PROTOCOL.md)** - Complete protocol specification with discovery, port assignment, and emergency stop
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
| **Development (one command!)** | `npm run dev` |
| Run native app (production) | `npm start` |
| Run JavaFX GUI | `mvn javafx:run` |
| Run simulator | `java -cp target/*-jar-with-dependencies.jar com.soccerbots.control.simulator.SimulatorApp` |
| Build everything | `npm run build:all` |
| Flash ESP32 firmware | Open `esp32_robot_firmware/minibots.ino` in Arduino IDE → Upload |
| Check robot status | Arduino Serial Monitor @ 115200 baud |
| Discovery port | UDP 12345 (firewall must allow) |
| Command port (static) | UDP 2367 (all robots) |
| Emergency stop | Click E-Stop button in Control Panel or send `ESTOP` via UDP |

### Protocol Ports Reference

| Port | Purpose | Direction |
|------|---------|-----------|
| 12345 | Robot discovery pings | ESP32 → Driver Station (broadcast) |
| 12345 | Emergency stop (ESTOP/ESTOP_OFF) | Driver Station → ESP32 (unicast) |
| 2367 | Movement commands | Driver Station → ESP32 (all robots, static port) |
| 2367 | Game state commands | Driver Station → ESP32 (all robots, static port) |
| 8080 | REST API | Frontend → Backend (localhost) |
| 8080 | WebSocket | Frontend ↔ Backend (localhost) |

---

**Choose your interface: Modern React UI, Classic JavaFX, or Lightweight Simulator** ✨
**Protocol: Automatic discovery with dynamic port assignment - zero configuration!** 🤖

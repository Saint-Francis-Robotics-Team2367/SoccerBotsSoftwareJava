# Project Structure Documentation

This document provides an overview of the SoccerBots Control Station project structure with the new Python backend.

## 📁 Directory Overview

```
SoccerBotsSoftwareJava/
├── python_backend/                       # Python backend (CURRENT)
│   ├── main.py                           # Entry point
│   ├── api_server.py                     # Flask REST API + WebSocket
│   ├── network_manager.py                # UDP networking
│   ├── robot_manager.py                  # Robot discovery & control
│   ├── robot.py                          # Robot data models
│   ├── controller_manager.py             # Controller support
│   ├── controller_input.py               # Input handling
│   ├── requirements.txt                  # Python dependencies
│   └── README.md                         # Backend documentation
├── frontend/                             # React + TypeScript UI
│   ├── src/components/                   # React components
│   ├── src/services/api.ts               # Backend API client
│   └── package.json                      # Frontend dependencies
├── electron/                             # Electron wrapper
│   ├── main.js                           # Main process
│   ├── preload.js                        # Context bridge
│   └── package.json                      # Electron dependencies
├── esp32_robot_firmware/                 # ESP32 Arduino firmware
│   ├── minibots.ino                      # Main firmware file
│   ├── minibot.cpp                       # Minibot class implementation
│   └── minibot.h                         # Minibot class header
├── legacy/                               # Old Java backend (DEPRECATED)
│   ├── src/                              # Java source code
│   ├── pom.xml                           # Maven configuration
│   └── README.md                         # Legacy documentation
├── package.json                          # Root build scripts
└── README.md                             # Main documentation
```

## 🐍 Python Backend Architecture

### Core Components

**1. NetworkManager** (`network_manager.py`)
- Manages UDP sockets for robot communication
- Handles discovery protocol (port 12345)
- Sends commands to ESP32 robots (port 2367)
- Emergency stop broadcasting

**2. RobotManager** (`robot_manager.py`)
- Robot discovery via UDP pings
- Connection management
- Command transmission
- Game state control (standby/teleop)

**3. ControllerManager** (`controller_manager.py`)
- Controller detection via pygame
- Input polling at 60Hz
- Controller-robot pairing
- Emergency stop control

**4. ApiServer** (`api_server.py`)
- Flask REST API endpoints
- WebSocket event broadcasting
- Match timer management
- Real-time status updates

### Data Models

**Robot** (`robot.py`)
- Robot state and properties
- Connection tracking
- Controller pairing

**ESP32Command** (`robot.py`)
- Command structure for ESP32
- Input normalization (-1.0 to 1.0 → 0-255)
- Binary packet construction

**ControllerInput** (`controller_input.py`)
- Normalized controller input
- Deadzone handling
- Button state tracking

**GameController** (`controller_input.py`)
- Controller abstraction
- Type identification

## 🎨 Frontend Architecture

### React Components

**ConnectionPanel** - Robot connection management
**NetworkAnalysis** - Real-time charts
**ControlPanel** - Emergency stop
**TerminalMonitor** - Live logs
**ServiceLog** - Event tracking

### API Integration

**API Service** (`services/api.ts`)
- REST endpoint wrapper
- WebSocket connection
- Event handling

## 🤖 ESP32 Firmware

### Minibot Class

**minibot.cpp/minibot.h**
- WiFi connection management
- UDP command listening (port 2367)
- Discovery ping broadcasting (port 12345)
- Motor control interface
- Emergency stop handling
- Game state processing

## 📡 Communication Protocol

### Discovery (Port 12345)
- Robots broadcast: `DISCOVER:<robotId>:<IP>`
- Backend listens passively
- Maintains discovered robots list

### Commands (Port 2367)

**Binary Movement (24 bytes):**
```
Bytes 0-15:  Robot name (null-padded)
Bytes 16-19: Axes (leftX, leftY, rightX, rightY) [0-255]
Bytes 20-21: Unused
Byte 22:     Buttons (cross, circle, square, triangle)
Byte 23:     Unused
```

**Text Commands:**
- `<name>:teleop` - Enable movement
- `<name>:standby` - Disable movement
- `ESTOP` - Emergency stop (broadcast)
- `ESTOP_OFF` - Release emergency stop

## 🔌 API Endpoints

### Health & Robots
- `GET /api/health` - Health check
- `GET /api/robots` - List all robots
- `POST /api/robots/{id}/connect` - Connect robot
- `POST /api/robots/{id}/disconnect` - Disconnect robot

### Controllers
- `GET /api/controllers` - List controllers
- `POST /api/controllers/{id}/pair/{robotId}` - Pair controller
- `POST /api/controllers/{id}/unpair` - Unpair controller

### Emergency & Match
- `POST /api/emergency-stop` - Activate E-stop
- `POST /api/match/start` - Start match
- `POST /api/match/stop` - Stop match
- `GET /api/match/timer` - Get timer state

## 🔄 Data Flow

### Controller Input Flow
```
USB Controller → pygame → ControllerManager →
ControllerInput → RobotManager → ESP32Command →
NetworkManager → UDP → ESP32 Robot
```

### Discovery Flow
```
ESP32 Robot → UDP Broadcast (port 12345) →
NetworkManager → RobotManager → Discovered Robots →
API Server → WebSocket → Frontend
```

### UI Update Flow
```
System Events → Manager Classes → API Server →
WebSocket → Frontend → React State → UI Update
```

## 🛠️ Build & Deployment

### Development
```bash
npm run dev  # Start all components
```

### Production
```bash
npm run build:all  # Build backend + frontend
npm start          # Run production build
```

### Backend Only
```bash
cd python_backend
python3 main.py
```

## 📝 Configuration Files

**package.json** - Root build scripts
**requirements.txt** - Python dependencies
**vite.config.ts** - Frontend build config
**electron/package.json** - Electron config

## 🔒 Legacy Java Code

The original Java backend has been moved to the `legacy/` directory. It is deprecated but preserved for reference. See `legacy/README.md` for details.

**Key Differences:**
| Feature | Python Backend | Java Backend |
|---------|---------------|--------------|
| Runtime | Python 3.8+ | Java 17+ JVM |
| Dependencies | pip | Maven |
| Startup time | ~2s | ~5-10s |
| Memory | ~50-100MB | ~200-300MB |
| Controller lib | pygame | JInput |

## 📚 Additional Resources

- **python_backend/README.md** - Detailed Python backend documentation
- **legacy/README.md** - Legacy Java backend information
- **README.md** - Main project documentation
- **QUICKSTART.md** - Quick start guide
- **ROBOT_PROTOCOL.md** - Detailed protocol specification

---

**Current Architecture: Python Backend + React Frontend + Electron Wrapper** ✨

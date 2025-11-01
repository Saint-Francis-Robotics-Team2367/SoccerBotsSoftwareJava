# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **SoccerBots Control Station** - a modern native desktop application for controlling ESP32-based soccer robots with a **Python backend**, **React frontend**, and **Electron wrapper**.

### Core Architecture

The system uses a **Python backend + React UI + Electron desktop** architecture:
- **Python Backend**: Flask REST API + WebSocket server, manages controllers, network, and robot communication
- **React Frontend**: TypeScript + Vite, modern UI with real-time updates
- **Electron Wrapper**: Native desktop app that launches Python backend and React UI
- **ESP32 Robots**: Receive UDP commands and execute movement/actions
- **Communication**: UDP broadcasts for discovery (port 12345), UDP unicast for commands (port 2367 - static)

### Key Components

#### Python Backend (`python_backend/`)
- **`main.py`**: Entry point, initializes all managers and API server
- **`api_server.py`**: Flask REST API + WebSocket event broadcasting
- **`network_manager.py`**: UDP networking (discovery on 12345, commands on 2367)
- **`robot_manager.py`**: Robot discovery, connection management, command sending
- **`robot.py`**: Robot data models and ESP32Command structure
- **`controller_manager.py`**: USB controller management via pygame
- **`controller_input.py`**: Input processing and normalization

#### React Frontend (`frontend/`)
- **`src/components/ConnectionPanel.tsx`**: Robot connection management
- **`src/components/NetworkAnalysis.tsx`**: Real-time latency and bandwidth charts
- **`src/components/ControlPanel.tsx`**: Emergency stop control
- **`src/components/TerminalMonitor.tsx`**: Live command output
- **`src/components/ServiceLog.tsx`**: Event tracking with timestamps
- **`src/services/api.ts`**: Backend API client and WebSocket integration

#### Electron (`electron/`)
- **`main.js`**: Main process, launches Python backend subprocess
- **`preload.js`**: Context bridge for secure IPC

#### ESP32 Firmware (`esp32_robot_firmware/`)
- **`minibots.ino`**: Arduino firmware entry point
- **`minibot.cpp/h`**: Minibot class with WiFi, UDP, motor control

### Data Flow

1. **Controller Input**: USB Controllers → pygame → ControllerManager → ControllerInput → RobotManager → ESP32Command → NetworkManager → UDP → ESP32
2. **Robot Discovery**: ESP32 → UDP Broadcast (12345) → NetworkManager → RobotManager → API Server → WebSocket → Frontend
3. **UI Updates**: System Events → Manager Classes → API Server → WebSocket → Frontend → React State → UI Update

## Development Commands

### One-Command Development (Recommended)
```bash
# Install all dependencies (first time only)
npm run install:all

# Run everything with one command!
npm run dev
# This starts: Python backend (port 8080) + React dev server (port 5173) + Electron window
```

### Manual Development
```bash
# Python backend only
npm run dev:backend
# Or directly: cd python_backend && python3 main.py

# React frontend (Vite dev server)
npm run dev:frontend

# Electron window
npm run dev:electron
```

### Building and Running
```bash
# Build everything
npm run build:all

# Run production build
npm start

# Create distributable installers
npm run dist
# Output: electron/dist/ (platform-specific)
```

### Backend Only (Headless API Mode)
```bash
cd python_backend
python3 main.py          # Default port 8080
python3 main.py 9090     # Custom port
```

### Legacy Java Backend (Deprecated)
```bash
# Legacy backend moved to legacy/ directory
npm run dev:backend:legacy
# Or: cd legacy && mvn exec:java -Dexec.mainClass="com.soccerbots.control.HeadlessLauncher"
```

### ESP32 Firmware Upload
1. Open `esp32_robot_firmware/minibots.ino` in Arduino IDE
2. Configure robot name in firmware: `Minibot bot("YOUR_ROBOT_NAME_HERE");`
3. Select Board: "ESP32 Dev Module"
4. Select Port: Your ESP32's COM port
5. Click Upload

## Technology Stack

### Python Backend
- **Python 3.8+** (3.13+ supported)
- **Flask 3.0.0** for REST API + WebSocket
- **pygame 2.6.1** for game controller support (cross-platform)
- **Flask-SocketIO** for real-time events
- **netifaces** for network interface detection

### Frontend
- **React 18+** with TypeScript
- **Vite** for build tooling
- **Tailwind CSS** for styling
- **Recharts** for data visualization
- **shadcn/ui** components

### Desktop App
- **Electron** for native desktop packaging
- **Node.js 18+** runtime

### ESP32
- **Arduino IDE** with ESP32 board support
- **WiFi** library for network connectivity
- **WiFiUdp** for UDP communication

## Code Patterns

### Robot Communication
- Commands sent as binary (24 bytes) via UDP to port 2367 (static)
- Robot discovery uses broadcast/response pattern on port 12345
- All network operations are asynchronous
- Emergency stop sent to all robots via broadcast

### Controller Input
- pygame library polls USB controllers at ~60Hz
- Input mapped to normalized values (-1.0 to 1.0)
- ESP32 receives values as 0-255 (converted in backend)
- Controller-to-robot pairing managed in backend

### Error Handling
- Comprehensive logging throughout all components (SLF4J pattern)
- Network timeouts and reconnection logic
- Graceful degradation when controllers/robots disconnect
- Emergency stop persists until explicitly released

### API Integration
- REST endpoints for state management
- WebSocket for real-time event broadcasting
- JSON serialization for all data transfer
- CORS enabled for development

## ESP32 Integration

The `esp32_robot_firmware/` directory contains Arduino firmware that:
- Connects to WiFi networks using stored credentials (default: "WATCHTOWER" / "lancerrobotics")
- Broadcasts discovery pings: `DISCOVER:<name>:<IP>` every 2 seconds on port 12345
- Listens on static port 2367 for movement commands
- Supports emergency stop commands (`ESTOP` / `ESTOP_OFF`)
- Handles game state changes (`<name>:teleop` / `<name>:standby`)
- Stops motors after 5 seconds without commands (timeout safety)

### Binary Command Structure (24 bytes)
```
Bytes 0-15:  Robot name (null-padded)
Bytes 16-19: Axes (leftX, leftY, rightX, rightY) [0-255]
Bytes 20-21: Unused
Byte 22:     Buttons (cross, circle, square, triangle)
Byte 23:     Unused
```

## Development Notes

### Python Backend
- Main class: `main.py` initializes NetworkManager, RobotManager, ControllerManager, ApiServer
- Threading used for concurrent controller polling and network operations
- Port 8080 for REST API and WebSocket
- Health check endpoint: `GET /api/health`

### Frontend
- Built with Vite for fast HMR (Hot Module Replacement)
- WebSocket connection automatically reconnects on disconnect
- API base URL: `http://localhost:8080` (configurable in `api.ts`)
- Components use React hooks for state management

### Electron
- Main process spawns Python subprocess before loading UI
- Preload script provides secure IPC bridge
- DevTools enabled in development mode
- Production builds bundle all dependencies

### Controller Support
- pygame supports PlayStation, Xbox, and generic USB controllers
- Windows: Works automatically
- Linux: User needs `/dev/input` access
- macOS: May need additional drivers for some controllers

### Network Requirements
- UDP ports 12345 (discovery) and 2367 (commands) must be accessible
- Firewall configuration required on some systems
- Robot and driver station must be on same subnet
- WiFi network must support UDP broadcast

## Recent Changes

### Migration to Python Backend
- **Complete rewrite**: Replaced Java backend with Python for better cross-platform support
- **Reduced startup time**: ~2s vs ~5-10s for Java
- **Lower memory footprint**: ~50-100MB vs ~200-300MB
- **Simplified dependencies**: pip vs Maven
- **Better controller support**: pygame vs JInput

### Build Issues Fixed
- **pygame compatibility**: Updated to 2.6.1 for Python 3.13+ support (fixes distutils.msvccompiler error)
- **Static port assignment**: All robots use port 2367 (no dynamic assignment needed)
- **Discovery protocol**: Robots broadcast discovery pings (passive listening on driver station)
- **Emergency stop**: Broadcast to all robots on discovery port

### Legacy Java Backend
- Moved to `legacy/` directory (deprecated but preserved for reference)
- Original Java source code in `legacy/src/`
- Maven build files in `legacy/pom.xml`
- See `legacy/README.md` for details

## API Reference

### REST Endpoints (Port 8080)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| GET | `/api/robots` | List all robots |
| POST | `/api/robots/{id}/connect` | Connect to robot |
| POST | `/api/robots/{id}/disconnect` | Disconnect robot |
| POST | `/api/robots/{id}/enable` | Enable robot (teleop) |
| POST | `/api/robots/{id}/disable` | Disable robot (standby) |
| POST | `/api/robots/refresh` | Scan for robots |
| GET | `/api/controllers` | List controllers |
| POST | `/api/controllers/{id}/pair/{robotId}` | Pair controller |
| POST | `/api/emergency-stop` | Activate emergency stop |
| POST | `/api/emergency-stop/deactivate` | Release emergency stop |
| GET | `/api/network/stats` | Network statistics |

### WebSocket Events (ws://localhost:8080/ws)
- `robot_connected` - Robot connected
- `robot_disconnected` - Robot disconnected
- `robot_discovered` - New robot found
- `emergency_stop` - Emergency stop state changed
- `robot_enabled` / `robot_disabled` - Robot state changed
- `controller_connected` / `controller_disconnected` - Controller state

## Troubleshooting

### Python Backend Issues
- Ensure Python 3.8+ installed: `python3 --version`
- Install dependencies: `cd python_backend && pip3 install -r requirements.txt`
- Check port 8080 availability
- View logs in terminal or Electron DevTools

### pygame Build Issues (Python 3.13+)
- **Error**: `ModuleNotFoundError: No module named 'distutils.msvccompiler'`
- **Fix**: Use pygame 2.6.1 or later (includes pre-built wheels)
- **Update**: Already fixed in requirements.txt

### Controller Not Detected
- Windows: Should work automatically
- Linux: `sudo usermod -a -G input $USER` (requires logout)
- macOS: May need additional drivers
- Verify pygame: `python3 -c "import pygame; print('OK')"`

### Robot Discovery Issues
- Check ESP32 Serial Monitor for "Connected! IP: X.X.X.X"
- Verify "Sent discovery ping" messages
- Ensure firewall allows UDP port 12345
- Confirm robot and driver station on same subnet
- Look for discovery pings in Terminal Monitor

### Emergency Stop Won't Release
- Click E-Stop button (sends ESTOP_OFF)
- Check Terminal Monitor for command confirmation
- Verify robot Serial Monitor shows "Emergency stop released"
- Last resort: Power cycle robot (clears E-Stop state)

## Additional Documentation

- **README.md** - Main project documentation
- **QUICKSTART.md** - Quick start guide
- **ROBOT_PROTOCOL.md** - Detailed protocol specification
- **ELECTRON_APP_README.md** - Electron app guide
- **python_backend/README.md** - Python backend details
- **legacy/README.md** - Legacy Java backend information

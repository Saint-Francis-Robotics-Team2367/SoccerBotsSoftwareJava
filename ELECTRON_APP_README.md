# SoccerBots Control System - Native Desktop Application

A modern, native desktop application for controlling ESP32-based soccer robots with a sleek React UI and Java backend.

## Architecture

The application consists of three main components:

1. **Java Backend** (`src/main/java`) - Handles robot communication, controller input, and network management
2. **React Frontend** (`frontend/`) - Modern UI built with React, TypeScript, and Tailwind CSS
3. **Electron Wrapper** (`electron/`) - Packages everything as a native desktop app

### Technology Stack

- **Backend**: Java 17, Javalin (HTTP/WebSocket server), JInput (controller support)
- **Frontend**: React 18, TypeScript, Vite, Tailwind CSS, Radix UI
- **Desktop**: Electron 28

## Prerequisites

- **Java JDK 17+** (for backend)
- **Node.js 18+** and npm (for frontend and Electron)
- **Maven 3.8+** (for building Java backend)

## Installation

### 1. Install Dependencies

```bash
# Install all dependencies (frontend + electron)
npm run install:all
```

### 2. Build the Backend

```bash
# Build Java backend JAR with dependencies
npm run build:backend
```

This creates: `target/robotics-control-system-1.0.0-jar-with-dependencies.jar`

### 3. Build the Frontend

```bash
# Build optimized React frontend
npm run build:frontend
```

This creates: `frontend/dist/`

## Running the Application

### Development Mode

For development, you'll need 3 terminals:

**Terminal 1 - Backend:**
```bash
npm run dev:backend
```

**Terminal 2 - Frontend (Vite dev server):**
```bash
npm run dev:frontend
```

**Terminal 3 - Electron:**
```bash
npm run dev:electron
```

The Electron window will load the frontend from `http://localhost:5173` and connect to the backend API at `http://localhost:8080`.

### Production Mode

```bash
# Build everything and run
npm start
```

This builds both backend and frontend, then launches Electron with the packaged app.

## Building Distributable App

```bash
# Build distributable for current platform
npm run dist
```

This creates platform-specific installers in `electron/dist/`:
- **Windows**: `.exe` installer
- **macOS**: `.dmg` installer
- **Linux**: `.AppImage`

## API Endpoints

The Java backend exposes these endpoints:

### REST API
- `GET /api/health` - Health check
- `GET /api/robots` - List all discovered robots
- `GET /api/robots/{id}` - Get robot by ID
- `POST /api/robots/{id}/connect` - Connect to robot
- `POST /api/robots/{id}/disconnect` - Disconnect from robot
- `POST /api/robots/{id}/enable` - Enable robot
- `POST /api/robots/{id}/disable` - Disable robot
- `POST /api/robots/refresh` - Scan for robots
- `GET /api/controllers` - List connected game controllers
- `POST /api/emergency-stop` - Activate emergency stop
- `POST /api/emergency-stop/deactivate` - Deactivate emergency stop
- `GET /api/network/stats` - Get network statistics

### WebSocket
- `ws://localhost:8080/ws` - Real-time updates for:
  - Robot connection/disconnection events
  - Emergency stop state changes
  - System logs and notifications

## Features

### Robot Control Panel
- **Connection Management**: Connect/disconnect robots via UDP
- **Status Monitoring**: Real-time robot status, signal strength, IP addresses
- **Enable/Disable**: Temporarily disable robots without disconnecting

### Network Analysis
- **Real-time Charts**: Latency and bandwidth visualization
- **Connection Statistics**: Track active connections and network health

### Control Panel
- **Emergency Stop**: Instantly halt all robot operations
- **System Controls**: Quick access to critical functions

### Service Log
- **Event Tracking**: All system events with timestamps
- **Log Levels**: Success, Info, Warning, Error
- **Clear History**: Manage log clutter

### Terminal Monitor
- **Command Output**: Real-time system command output
- **Status Updates**: Connection events, errors, and notifications

## Project Structure

```
SoccerBotsSoftwareJava/
├── src/main/java/com/soccerbots/control/
│   ├── api/                  # HTTP REST API & WebSocket server
│   │   └── ApiServer.java
│   ├── controller/           # Game controller input
│   ├── network/              # WiFi and UDP networking
│   ├── robot/                # Robot communication
│   ├── gui/                  # Legacy Swing GUI (optional)
│   ├── simulator/            # 3D robot simulator
│   ├── HeadlessLauncher.java # Main entry point for API mode
│   └── Launcher.java         # Legacy GUI launcher
├── frontend/
│   ├── src/
│   │   ├── components/       # React UI components
│   │   ├── services/         # API service layer
│   │   │   └── api.ts        # Backend API client
│   │   ├── App.tsx           # Main app component
│   │   └── main.tsx          # Entry point
│   ├── package.json
│   └── vite.config.ts
├── electron/
│   ├── main.js               # Electron main process
│   ├── preload.js            # Context bridge
│   └── package.json
├── esp32_robot_firmware/     # ESP32 Arduino code
├── package.json              # Root build scripts
└── pom.xml                   # Maven configuration
```

## Configuration

### Backend Port
Default: `8080`

To change, modify `electron/main.js`:
```javascript
const API_PORT = 8080;
```

### Frontend Development Port
Default: `5173` (Vite default)

To change, modify `frontend/vite.config.ts`:
```typescript
server: {
  port: 5173,
}
```

## Troubleshooting

### Java Backend Won't Start
- Ensure Java 17+ is installed: `java -version`
- Check if port 8080 is available
- View backend logs in Electron DevTools console

### Frontend Won't Load
- Ensure frontend built successfully: check `frontend/dist/`
- In dev mode, ensure Vite dev server is running on port 5173

### Controllers Not Detected
- Install JInput native libraries (should be automatic with Maven)
- On Windows, ensure DirectInput drivers are installed
- Try refreshing controller list in the app

### Robots Not Discovered
- Ensure robots and host are on same WiFi network
- Check firewall allows UDP ports 12345-12346
- Verify robot firmware is running and configured

### WebSocket Connection Failed
- Backend must be running before frontend loads
- Check browser console for connection errors
- Verify API URL in `frontend/src/services/api.ts`

## Development Tips

### Hot Reload
- Frontend: Automatic with Vite dev server
- Backend: Restart `npm run dev:backend` after Java changes
- Electron: Automatic restart on main.js changes

### Debugging
- **Frontend**: Open DevTools in Electron window (Ctrl+Shift+I / Cmd+Option+I)
- **Backend**: Use your IDE's debugger or add logging
- **Electron**: Check terminal output for main process logs

### Adding New API Endpoints
1. Add endpoint in `src/main/java/com/soccerbots/control/api/ApiServer.java`
2. Add method in `frontend/src/services/api.ts`
3. Use in React components via `apiService.yourMethod()`

## License

MIT License - See LICENSE file for details

## Credits

- **UI Design**: Based on RobotControlDesktopApp Figma template
- **Framework**: Built with React, Electron, and Java

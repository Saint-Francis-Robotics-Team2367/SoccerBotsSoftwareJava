# Python Backend - SoccerBots Control System

Modern Python backend for the SoccerBots robot control system with REST API and WebSocket support.

## Features

- **REST API** - Full robot and controller management via HTTP endpoints
- **WebSocket** - Real-time updates and event streaming
- **UDP Communication** - ESP32 robot discovery and command transmission
- **Controller Support** - PlayStation, Xbox, and generic controllers via pygame
- **Cross-Platform** - Works on Windows, macOS, and Linux
- **Lightweight** - No JVM required, simple Python dependencies

## Requirements

- Python 3.8 or later
- pip (Python package manager)

## Installation

```bash
# Install dependencies
pip3 install -r requirements.txt
```

### Dependencies

- **Flask** - Web framework for REST API
- **Flask-SocketIO** - WebSocket support
- **pygame** - Game controller support
- **netifaces** - Network interface detection

## Running the Backend

```bash
# Run with default port (8080)
python3 main.py

# Run with custom port
python3 main.py 9090
```

The server will start and listen on:
- REST API: `http://localhost:8080/api/`
- WebSocket: `ws://localhost:8080/`

## Architecture

### Core Components

1. **NetworkManager** (`network_manager.py`)
   - Manages UDP sockets for robot communication
   - Handles discovery protocol (port 12345)
   - Sends commands to ESP32 robots (port 2367)
   - Emergency stop broadcasting

2. **RobotManager** (`robot_manager.py`)
   - Robot discovery via UDP pings
   - Connection management
   - Command transmission
   - Game state control (standby/teleop)

3. **ControllerManager** (`controller_manager.py`)
   - Controller detection via pygame
   - Input polling at 60Hz
   - Controller-robot pairing
   - Emergency stop control

4. **ApiServer** (`api_server.py`)
   - Flask REST API endpoints
   - WebSocket event broadcasting
   - Match timer management
   - Real-time status updates

### Data Models

- **Robot** (`robot.py`) - Robot state and properties
- **ESP32Command** (`robot.py`) - Command structure for ESP32
- **ControllerInput** (`controller_input.py`) - Normalized controller input
- **GameController** (`controller_input.py`) - Controller abstraction

## API Endpoints

### Health & Status

- `GET /api/health` - Health check

### Robots

- `GET /api/robots` - List all robots (discovered + connected)
- `GET /api/robots/{id}` - Get robot details
- `POST /api/robots/{id}/connect` - Connect to discovered robot
- `POST /api/robots/{id}/disconnect` - Disconnect from robot
- `POST /api/robots/{id}/enable` - Enable robot
- `POST /api/robots/{id}/disable` - Disable robot
- `POST /api/robots/refresh` - Trigger robot scan

### Controllers

- `GET /api/controllers` - List all controllers
- `POST /api/controllers/{id}/pair/{robotId}` - Pair controller with robot
- `POST /api/controllers/{id}/unpair` - Unpair controller
- `POST /api/controllers/{id}/enable` - Enable controller
- `POST /api/controllers/{id}/disable` - Disable controller
- `POST /api/controllers/refresh` - Trigger controller scan

### Emergency Stop

- `POST /api/emergency-stop` - Activate emergency stop
- `POST /api/emergency-stop/deactivate` - Deactivate emergency stop

### Match Timer

- `GET /api/match/timer` - Get timer state
- `POST /api/match/start` - Start match
- `POST /api/match/stop` - Stop match
- `POST /api/match/reset` - Reset timer
- `POST /api/match/duration` - Set match duration (seconds)

### Network

- `GET /api/network/stats` - Get network statistics

## WebSocket Events

The server broadcasts these events via WebSocket:

- `robot_connected` - Robot connected
- `robot_disconnected` - Robot disconnected
- `robot_enabled` / `robot_disabled` - Robot state changed
- `controller_paired` / `controller_unpaired` - Controller pairing changed
- `controller_enabled` / `controller_disabled` - Controller state changed
- `controllers_updated` - Controller count changed
- `emergency_stop` - Emergency stop state changed
- `match_start` / `match_stop` / `match_end` - Match state
- `timer_update` - Timer countdown update (every second)

## ESP32 Communication Protocol

### Discovery (Port 12345)

Robots broadcast: `DISCOVER:<robotId>:<IP>`

The backend listens passively and maintains a list of discovered robots.

### Commands (Port 2367)

**Binary Movement Commands (24 bytes):**
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
- `ESTOP_OFF` - Release emergency stop (broadcast)

## Controller Support

The backend uses pygame for cross-platform controller support:

### Supported Controllers

- PlayStation 4 DualShock
- PlayStation 5 DualSense
- Xbox One / Series controllers
- Generic USB gamepads

### Input Mapping

- **Left Stick**: Movement control
- **Right Stick**: Rotation control
- **Buttons**: Mapped to robot actions
- **Deadzone**: 10% to prevent drift
- **Polling Rate**: 60Hz for responsive input

### Platform-Specific Notes

**Linux:**
```bash
# Add user to input group for controller access
sudo usermod -a -G input $USER
# Log out and back in
```

**Windows:**
Controllers work automatically with pygame.

**macOS:**
May require additional drivers for some controllers.

## Development

### Project Structure

```
python_backend/
├── main.py                  # Entry point
├── api_server.py            # Flask REST API + WebSocket
├── network_manager.py       # UDP networking
├── robot_manager.py         # Robot discovery & control
├── robot.py                 # Robot data models
├── controller_manager.py    # Controller support
├── controller_input.py      # Input handling
├── requirements.txt         # Python dependencies
└── __init__.py              # Package initialization
```

### Running Tests

```bash
# Test imports
python3 -c "import flask; import pygame; import netifaces; print('All imports OK')"

# Test backend startup
python3 main.py
# Should see: "API server running on http://localhost:8080"
```

### Debugging

Enable debug logging:
```python
# In main.py, change logging level
logging.basicConfig(level=logging.DEBUG, ...)
```

## Integration with Frontend

The Python backend provides the same API as the original Java backend, so the Electron + React frontend works without modifications.

The frontend connects to:
- REST API: `http://localhost:8080/api/`
- WebSocket: `ws://localhost:8080/`

## Performance

- **Startup time**: < 2 seconds
- **Memory usage**: ~50-100 MB
- **Controller polling**: 60Hz (~16ms)
- **Robot discovery**: Passive, no overhead
- **API latency**: < 10ms for local requests

## Comparison with Java Backend

| Feature | Python Backend | Java Backend |
|---------|---------------|--------------|
| Runtime | Python 3.8+ | Java 17+ JVM |
| Dependencies | pip (Flask, pygame) | Maven (JavaFX, JInput) |
| Startup time | ~2 seconds | ~5-10 seconds |
| Memory usage | ~50-100 MB | ~200-300 MB |
| Controller support | pygame (cross-platform) | JInput (Windows-focused) |
| Development | Simpler, more accessible | More verbose |

The Python backend is **recommended** for new deployments.

## License

MIT License - Same as the main project.

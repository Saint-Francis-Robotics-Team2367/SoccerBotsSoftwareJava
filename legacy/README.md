# Legacy Java Backend

This directory contains the original Java backend implementation of the SoccerBots Control System.

## Status

**⚠️ DEPRECATED** - This Java backend has been replaced by a Python implementation in `/python_backend/`.

The Python backend provides:
- **Exact same functionality** as the Java version
- **Better PlayStation controller support** via pygame
- **Simpler deployment** - no Maven or JVM required
- **Easier maintenance** - Python is more accessible for robotics teams

## Original Implementation

The Java backend was built using:
- Java 17
- JavaFX for GUI
- JInput for controller support
- Javalin for REST API and WebSocket
- Maven for build management

## Components

- `src/main/java/com/soccerbots/control/` - Main application code
  - `api/` - REST API and WebSocket server
  - `controller/` - Game controller input handling
  - `network/` - UDP communication with ESP32 robots
  - `robot/` - Robot management and discovery
  - `gui/` - JavaFX user interface
  - `simulator/` - 3D robot simulator

## Building (Legacy)

If you need to run the Java backend for any reason:

```bash
# Build
mvn clean compile assembly:single

# Run headless mode (API server)
mvn exec:java -Dexec.mainClass="com.soccerbots.control.HeadlessLauncher"

# Run JavaFX GUI
mvn javafx:run
```

## Migration Notes

The Python backend (`/python_backend/`) implements all the same endpoints and protocols:
- UDP discovery on port 12345
- ESP32 commands on port 2367
- REST API on port 8080
- WebSocket for real-time updates
- Emergency stop functionality
- Match timer
- Controller pairing

The frontend (Electron + React) works identically with both backends.

## Recommendation

**Use the Python backend** (`/python_backend/`) for all new development and deployments.

This Java code is kept for reference only.

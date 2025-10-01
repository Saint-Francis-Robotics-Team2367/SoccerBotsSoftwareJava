# File Reference - SoccerBots Control Station

This document provides a comprehensive file-by-file explanation of the SoccerBots Control Station project, detailing what each file does and how it contributes to the overall system.

## 📂 Project Root Files

### `pom.xml`
**Type**: Maven Build Configuration
**Purpose**: Defines project dependencies, build settings, and packaging configuration
**Key Features**:
- JavaFX 21.0.1 dependencies for modern UI
- JInput library for USB controller support
- SLF4J + Logback for logging
- Assembly plugin for creating fat JAR
- Java 17 compilation target

**Critical Dependencies**:
```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21.0.1</version>
</dependency>
```

### `README.md`
**Type**: Main Documentation
**Purpose**: Primary project documentation with Grok theme overview
**Contents**:
- Grok AI-inspired design features
- ESP32 integration details
- Installation and setup instructions
- Usage examples and troubleshooting

### `USER_MANUAL.md`
**Type**: User Documentation
**Purpose**: Comprehensive user guide for operating the control station
**Contents**:
- Interface walkthrough with Grok theme
- Step-by-step operational procedures
- Robot and controller management
- Troubleshooting and best practices

### `PROJECT_STRUCTURE.md`
**Type**: Technical Documentation
**Purpose**: Detailed architectural overview and design patterns
**Contents**:
- Package organization and responsibilities
- Design patterns and architectural decisions
- Data flow and communication protocols
- Development guidelines

### `CLAUDE.md`
**Type**: Development Instructions
**Purpose**: Guidance for Claude Code when working with the project
**Contents**:
- Project overview and architecture
- Build commands and development workflow
- Key patterns and conventions

---

## ☕ Java Source Code (`src/main/java/com/soccerbots/control/`)

### Application Entry Point

#### `RoboticsControlFXApp.java`
**Package**: `com.soccerbots.control`
**Type**: JavaFX Application Class
**Purpose**: Main application launcher with Grok theme integration

**What it does**:
- Extends JavaFX `Application` class
- Initializes main window (1400x900, min 1200x800)
- Loads Grok CSS theme from resources
- Sets up application shutdown handlers
- Handles JavaFX lifecycle management

**Key Methods**:
- `start(Stage primaryStage)` - JavaFX application initialization
- `main(String[] args)` - Application entry point with logging

**Dependencies**: MainWindow, JavaFX Platform, Logging

---

## 🎨 User Interface Package (`gui/`)

### Core Window Management

#### `MainWindow.java`
**Type**: Primary UI Controller
**Purpose**: Main application window with Grok-inspired navigation

**What it does**:
- Creates BorderPane layout with header, content, footer
- Implements pill-shaped navigation buttons with hover animations
- Manages panel switching with fade/scale transitions
- Provides emergency stop functionality
- Updates status bar with real-time connection counts

**UI Components**:
- Header bar with navigation pills and emergency stop
- Content area with animated panel transitions
- Status bar with live system metrics

**Key Features**:
- Grok theme integration throughout
- 150-200ms smooth animations
- Emergency stop with confirmation dialog
- Real-time status updates every second

**Dependencies**: All panel classes, managers, JavaFX controls

### Robot Management Interface

#### `RobotPanel.java`
**Type**: Robot Management UI
**Purpose**: ESP32 robot addition, monitoring, and control

**What it does**:
- Displays WATCHTOWER network connection status
- Provides manual robot addition dialog
- Shows robot cards with live status indicators
- Controls teleop/standby game state
- Manages robot actions (test, stop, remove)

**UI Elements**:
- Network status section with connection info
- Add robot dialog with name/IP inputs
- Robot cards with status circles and action buttons
- Teleop control toggle button

**Visual Features**:
- Grok-styled cards with hover animations (1.02x scale)
- Color-coded status indicators (green/red circles)
- Smooth card transitions and loading animations
- Professional typography with monospace IPs

**Dependencies**: RobotManager, Robot data model, JavaFX controls

#### `ControllerPanel.java`
**Type**: Controller Management UI
**Purpose**: USB controller detection, pairing, and monitoring

**What it does**:
- Displays auto-detected USB controllers
- Shows real-time controller input visualization
- Manages controller-robot pairing
- Provides manual refresh functionality
- Monitors controller connection status

**Controller Support**:
- Xbox One/Series controllers
- PlayStation 4/5 controllers
- Generic DirectInput gamepads
- Any controller with dual analog sticks

**Visual Features**:
- Live input visualization (sticks, buttons, triggers)
- Pairing status indicators
- Refresh button with loading animation
- Grok-themed controller cards

**Dependencies**: ControllerManager, GameController, JavaFX controls

#### `NetworkPanel.java`
**Type**: Network Management UI
**Purpose**: WATCHTOWER network monitoring and connection testing

**What it does**:
- Monitors current WiFi network connection
- Displays WATCHTOWER network requirements
- Shows ESP32 communication port (2367)
- Provides direct robot connection testing
- Updates network status in real-time

**Network Features**:
- WATCHTOWER network detection
- Connection quality monitoring
- Direct IP connectivity testing
- Network diagnostic information

**Visual Elements**:
- Network status cards with color coding
- Connection test interface
- ESP32 protocol information display
- Real-time status updates

**Dependencies**: NetworkManager, JavaFX controls

#### `MonitoringPanel.java`
**Type**: System Monitoring UI
**Purpose**: Real-time system performance and status monitoring

**What it does**:
- Displays system performance metrics
- Monitors network latency and throughput
- Shows robot communication status
- Tracks controller input responsiveness
- Provides historical data visualization

**Monitoring Features**:
- Real-time metrics dashboard
- Connection health indicators
- Performance graphs and charts
- System resource monitoring

#### `SettingsPanel.java`
**Type**: Application Settings UI
**Purpose**: Configuration and preferences management

**What it does**:
- Provides theme customization options
- Manages application preferences
- Controls logging and debug settings
- Handles controller configuration
- Manages network settings

**Configuration Options**:
- Grok theme color customization
- Animation speed controls
- Network timeout settings
- Logging level configuration

### Dialog Components

#### `WiFiConfigDialog.java`
**Type**: Information Dialog
**Purpose**: ESP32 network information display (updated for ESP32)

**What it does**:
- Shows ESP32 network requirements
- Displays WATCHTOWER connection status
- Provides informational guidance for robot setup
- Uses Grok-themed dialog styling

**ESP32 Focus**:
- No longer supports WiFi configuration (ESP32 pre-configured)
- Provides information about WATCHTOWER network
- Explains ESP32 communication requirements
- Offers troubleshooting guidance

---

## 🌐 Network Communication Package (`network/`)

#### `NetworkManager.java`
**Type**: Network Communication Handler
**Purpose**: ESP32 UDP communication and network management

**What it does**:
- Manages UDP socket on port 2367 for ESP32 communication
- Sends binary command packets (24 bytes)
- Monitors WATCHTOWER network connection
- Handles network status detection (Windows-specific)
- Provides network diagnostics

**ESP32 Protocol**:
- **Port**: 2367 (constant ESP32_UDP_PORT)
- **Network**: WATCHTOWER (constant EXPECTED_WIFI_NETWORK)
- **Packet Format**: 24-byte binary structure
- **Command Types**: Binary movement commands, text status commands

**Binary Packet Structure**:
```
Bytes 0-15:  Robot name (null-padded string)
Bytes 16-17: Left stick axes (leftX, leftY) [0-255]
Bytes 18-19: Right stick axes (rightX, rightY) [0-255]
Bytes 20-21: Reserved axes
Byte 22:     Button flags (Cross, Circle, Square, Triangle)
Byte 23:     Reserved buttons
```

**Key Methods**:
- `sendRobotCommand()` - Binary UDP transmission to robots
- `sendGameStatus()` - Text-based status commands
- `isConnectedToExpectedNetwork()` - WATCHTOWER verification
- `checkCurrentNetworkStatus()` - Windows network detection

**Dependencies**: Java networking, concurrent execution, logging

---

## 🤖 Robot Management Package (`robot/`)

### Core Robot Management

#### `RobotManager.java`
**Type**: Robot Lifecycle Manager
**Purpose**: ESP32 robot registration, communication, and state management

**What it does**:
- Manages robot registration and removal (manual addition)
- Controls game state (teleop/standby) for all robots
- Processes controller input and converts to ESP32 commands
- Implements emergency stop functionality
- Tracks robot connection status and cleanup

**Game State Management**:
- **Standby Mode**: No movement commands sent (safety)
- **Teleop Mode**: Full controller input processing
- **Emergency Stop**: Immediate halt of all robots

**Key Operations**:
- Manual robot addition with name and IP
- Controller input to ESP32 command conversion
- Real-time robot status monitoring
- Offline robot cleanup (60-second timeout)

**ESP32 Integration**:
- Direct IP-based robot targeting
- Binary command protocol support
- WATCHTOWER network dependency
- Real-time command transmission

**Dependencies**: NetworkManager, ESP32Command, Robot model, ControllerInput

#### `Robot.java`
**Type**: Robot Data Model
**Purpose**: Individual robot state and information storage

**What it does**:
- Stores robot identification (name, ID, IP address)
- Tracks connection status and last seen timestamp
- Manages controller pairing information
- Provides robot state query methods
- Calculates time since last communication

**Robot Properties**:
- `name` - Human-readable display name
- `id` - Unique robot identifier
- `ipAddress` - Network IP address for communication
- `lastSeenTime` - Timestamp of last successful communication
- `pairedControllerId` - Associated controller identifier
- `status` - Current connection status

**Utility Methods**:
- `isConnected()` - Connection status based on recent communication
- `getTimeSinceLastSeen()` - Milliseconds since last contact
- `updateLastSeenTime()` - Updates communication timestamp

#### `ESP32Command.java`
**Type**: Binary Command Structure
**Purpose**: ESP32-specific command formatting and binary protocol

**What it does**:
- Converts controller input (-1.0 to 1.0) to ESP32 format (0-255)
- Implements PlayStation-style button mapping
- Constructs 24-byte binary packets for transmission
- Provides movement threshold detection
- Creates neutral/stop commands

**Controller Mapping**:
- **Left Stick**: leftX, leftY (movement axes)
- **Right Stick**: rightX, rightY (rotation, reserved)
- **Buttons**: Cross (A), Circle (B), Square (X), Triangle (Y)
- **Center Values**: leftX=125, leftY=130, rightX=127, rightY=130

**Binary Protocol**:
- Converts normalized values to 0-255 range
- Inverts Y-axis for proper robot movement
- Packs button states into bit flags
- Null-pads robot name to 16 bytes

**Key Features**:
- Movement threshold detection (10% deadzone)
- Button input validation
- Normalized value getters for debugging
- Stop command generation

**Dependencies**: Math utilities, string processing

---

## 🎮 Controller Input Package (`controller/`)

### Input Management

#### `ControllerManager.java`
**Type**: USB Controller Manager
**Purpose**: Controller detection, polling, and input processing

**What it does**:
- Automatically detects USB controllers every 5 seconds
- Polls controller input at 60Hz (16ms intervals)
- Manages controller-robot pairing assignments
- Processes raw input and sends to robots
- Handles emergency stop integration

**Controller Detection**:
- Scans for DirectInput compatible devices
- Filters for gamepads, sticks, and controllers
- Tests controller polling capability
- Generates unique controller IDs
- Removes disconnected controllers

**Input Processing Flow**:
1. Poll all connected controllers (60Hz)
2. Read stick positions and button states
3. Convert to ControllerInput objects
4. Check for robot pairing
5. Send movement commands to paired robots
6. Handle emergency stop conditions

**Supported Hardware**:
- Xbox One/Series controllers (USB)
- PlayStation 4/5 controllers (USB)
- Generic DirectInput gamepads
- Any controller with dual analog sticks

**Dependencies**: JInput library, RobotManager, ControllerInput, concurrent execution

#### `GameController.java`
**Type**: Controller Abstraction
**Purpose**: Individual controller wrapper and state management

**What it does**:
- Wraps JInput Controller objects
- Stores controller identification and metadata
- Caches last known input state
- Provides connection status monitoring
- Manages controller-specific configuration

**Controller Information**:
- Unique controller ID
- Device name and type
- Connection status
- Last input state
- Hardware capabilities

**State Management**:
- Input caching for comparison
- Connection monitoring
- Error state handling
- Hardware capability detection

#### `ControllerInput.java`
**Type**: Input Data Structure
**Purpose**: Normalized controller input representation

**What it does**:
- Stores normalized stick positions (-1.0 to 1.0)
- Manages 16-button state array
- Applies deadzone to analog inputs (10% default)
- Provides movement threshold detection (5% default)
- Offers convenience methods for common operations

**Input Components**:
- **Left Stick**: X/Y positions with deadzone
- **Right Stick**: X/Y positions with deadzone
- **Triggers**: Left/right analog trigger values
- **D-Pad**: Hat switch position
- **Buttons**: 16-button boolean array

**Value Processing**:
- Deadzone application (10% threshold)
- Movement threshold detection (5% threshold)
- Y-axis inversion for intuitive control
- Button state debouncing

**Convenience Methods**:
- `getForward()` - Forward/backward movement (-leftStickY)
- `getSideways()` - Left/right movement (leftStickX)
- `getRotation()` - Rotation control (rightStickX)
- `hasMovement()` - Significant movement detection
- `isStopCommand()` - Neutral position detection

---

## 🎨 Resources (`src/main/resources/`)

### Styling

#### `styles/grok.css`
**Type**: CSS Stylesheet
**Purpose**: Grok AI-inspired theme implementation

**What it does**:
- Defines comprehensive CSS variable system for theming
- Implements dark-mode color palette
- Provides modern typography using Inter font family
- Creates smooth animations (150-200ms timing)
- Ensures responsive design across screen sizes

**Color System**:
```css
/* Primary colors */
-primary-bg: #0A0A0A        /* Deep black background */
-secondary-bg: #1A1A1A      /* Card backgrounds */
-tertiary-bg: #333333       /* Button backgrounds */

/* Text colors */
-primary-text: #FFFFFF      /* High contrast white */
-secondary-text: #EDEDED    /* Off-white for readability */
-muted-text: #9CA3AF        /* Subtle gray for captions */

/* Accent colors */
-accent-blue: #1D9BF0       /* Interactive elements */
-success-green: #22C55E     /* Success states */
-warning-yellow: #EAB308    /* Warning states */
-error-red: #EF4444         /* Error states */
```

**Typography System**:
- Font family: Inter, SF Pro Display, Roboto, system fonts
- Font sizes: 12px (small), 14px (body), 16px (labels), 24px (titles)
- Font weights: 400 (regular), 500 (medium), 600 (semi-bold)
- Letter spacing: Slight negative (-0.06em) for modern feel

**Component Classes**:
- `.grok-card` - Main container styling with shadows
- `.grok-button` - Interactive buttons with variants (success, danger, etc.)
- `.grok-text-field` - Modern input fields with focus effects
- `.grok-title`, `.grok-subtitle`, `.grok-body` - Typography hierarchy
- `.robot-card`, `.controller-card` - Specialized card components

**Animation Framework**:
- Hover effects: 1.02x scaling with smooth transitions
- Focus states: Subtle glow effects
- Loading states: Fade and scale animations
- Transition timing: 150-200ms for responsiveness

**Responsive Design**:
- Flexible layouts using CSS Grid and Flexbox
- Responsive spacing with CSS variables
- Adaptive component sizing
- Mobile-friendly breakpoints

---

## 🔧 ESP32 Firmware (`esp32_robot_code/`)

### Robot Firmware Directory

#### `esp32_robot_code/` (Directory)
**Type**: Arduino Firmware Package
**Purpose**: ESP32 robot firmware for WATCHTOWER network communication

**What it contains**:
- Arduino sketch files (.ino)
- ESP32-specific library dependencies
- WATCHTOWER network configuration
- UDP command listener on port 2367
- Binary protocol parsing implementation

**Key Features**:
- Pre-configured for WATCHTOWER network
- UDP packet reception and parsing
- Binary command interpretation
- Movement command execution
- Robot identification and response

**Communication Protocol**:
- Listens on UDP port 2367
- Receives 24-byte binary packets
- Parses robot name, axes, and buttons
- Executes movement commands
- Provides status feedback

**Note**: This firmware is provided by the user and specifically designed to work with the Java control station's communication protocol.

---

## 🛠️ Build and Configuration

### Build Output (`target/`)

#### `target/classes/`
**Type**: Compiled Java Classes
**Purpose**: Compiled bytecode and resources

**Contents**:
- Compiled .class files from Java sources
- Copied resource files (CSS, configs)
- Package structure matching source organization

#### `target/robotics-control-system-1.0.0.jar`
**Type**: Standard JAR File
**Purpose**: Application JAR without dependencies

**Usage**: Development and testing with external classpath

#### `target/robotics-control-system-1.0.0-jar-with-dependencies.jar`
**Type**: Fat JAR File
**Purpose**: Self-contained executable with all dependencies

**Usage**: Production deployment and distribution
**Command**: `java -jar robotics-control-system-1.0.0-jar-with-dependencies.jar`

---

## 🔄 Key Interactions and Data Flow

### Application Startup Flow
1. `RoboticsControlFXApp.main()` → JavaFX launcher
2. `start()` method → Creates MainWindow
3. MainWindow → Initializes all managers (Network, Robot, Controller)
4. Managers → Start background threads and timers
5. CSS theme → Loads Grok styling
6. UI → Displays with smooth animations

### Controller Input Flow
1. USB controller → JInput library detection
2. ControllerManager → 60Hz polling loop
3. Raw input → ControllerInput normalization
4. Controller pairing → Robot targeting
5. ESP32Command → Binary packet creation
6. NetworkManager → UDP transmission to robot
7. ESP32 robot → Command execution

### Robot Management Flow
1. Manual addition → RobotPanel dialog
2. Robot creation → RobotManager registration
3. Network testing → UDP ping to robot IP
4. Status monitoring → Real-time connection tracking
5. UI updates → Live status card updates
6. Game state → Teleop/standby control

### UI Update Flow
1. Background timers → Status polling (1-2 second intervals)
2. Manager state changes → Event notifications
3. JavaFX Platform.runLater() → UI thread updates
4. CSS animations → Smooth visual transitions
5. Status indicators → Color-coded feedback

---

This file reference provides complete information about every component in the SoccerBots Control Station, enabling developers and users to understand the system architecture and locate specific functionality quickly.
# SoccerBots Control Station - User Manual

Welcome to the SoccerBots Control Station, featuring a modern Grok AI-inspired JavaFX interface for controlling ESP32-based soccer robots with professional dark-mode aesthetics and real-time controller input.

## 📖 Table of Contents
1. [Overview](#-overview)
2. [Getting Started](#-getting-started)
3. [Grok Interface Guide](#-grok-interface-guide)
4. [Robot Management](#-robot-management)
5. [Controller Management](#-controller-management)
6. [Network Configuration](#-network-configuration)
7. [System Monitoring](#-system-monitoring)
8. [Advanced Features](#-advanced-features)
9. [Troubleshooting](#-troubleshooting)
10. [Best Practices](#-best-practices)

## 🚀 Overview

The SoccerBots Control Station is a modern JavaFX application with Grok AI-inspired design for ESP32 robot control. The system features:

### 🎨 Modern Interface
- **Grok AI-Inspired Theme**: Dark-mode interface with cosmic aesthetics
- **Smooth Animations**: 150-200ms transitions with subtle scaling effects
- **Professional Typography**: Inter font family with optimized readability
- **Responsive Design**: Adapts to different screen sizes
- **High Accessibility**: Strong contrast ratios and clear visual hierarchy

### 🤖 ESP32 Integration
- **WATCHTOWER Network**: Dedicated network for ESP32 communication
- **Binary UDP Protocol**: High-performance 24-byte command packets
- **Manual Robot Addition**: Direct IP-based robot connections
- **Real-time Controller Mapping**: Direct stick-to-robot input translation
- **Game State Management**: Teleop/standby mode control

### 🎮 Controller Features
- **Auto-Detection**: USB controllers appear automatically
- **Multi-Controller Support**: Xbox, PlayStation, generic gamepads
- **Real-time Input**: 60Hz controller polling with live visualization
- **Emergency Stop**: Immediate safety control for all robots

## 🚀 Getting Started

### Prerequisites
- **Java 17+** with JavaFX support
- **WATCHTOWER WiFi network** access
- **ESP32 robots** pre-configured for WATCHTOWER
- **USB controllers** (Xbox, PlayStation, or compatible)

### First Launch
1. **Connect to WATCHTOWER Network**
   ```
   Connect your computer to the WATCHTOWER WiFi network
   Verify strong signal strength
   ```

2. **Launch Control Station**
   ```bash
   java -jar robotics-control-system-1.0.0-jar-with-dependencies.jar
   ```

3. **Interface Overview**
   - Grok-inspired dark interface loads
   - Navigation pills in header bar
   - Robots panel shown by default
   - Status bar displays connection info

### Quick Setup Process
1. **Verify Network**: Check WATCHTOWER connection in Network panel
2. **Add Robots**: Use "Add Robot" button with name and IP
3. **Connect Controllers**: Plug in USB controllers (auto-detected)
4. **Pair Devices**: Assign controllers to specific robots
5. **Enable Teleop**: Start teleop mode for robot control
6. **Begin Operation**: Use controllers to drive robots

## Network Management

### Host Your Own Network
**Best for**: Tournament settings, dedicated robot networks

1. **Select "Host Own Network"**
2. **Configure Settings**:
   - **SSID**: Network name (default: "SoccerBots_Network")
   - **Password**: Network password (minimum 8 characters)
3. **Click "Start Network"**
4. **Wait for confirmation** (green status indicator)

**Advantages**:
- Full control over network settings
- Isolated from other network traffic
- Optimized for low latency

**Requirements**:
- Administrator privileges (Windows)
- Compatible WiFi adapter
- No other hosted networks running

### Connect to Existing Network
**Best for**: Home use, shared environments

1. **Select "Connect to Existing Network"**
2. **Choose Network**: Select from available networks list
3. **Enter Password**: Provide network credentials
4. **Click "Start Network"**
5. **Wait for connection** (green status indicator)

**Advantages**:
- No special privileges required
- Uses existing network infrastructure
- Multiple devices can share connection

**Requirements**:
- 2.4GHz network for ESP32 compatibility
- Same network for host and robots
- UDP multicast support

### Network Status Indicators
- **Red "Network: Disconnected"**: No active network connection
- **Green "Network: Active (Hosting)"**: Successfully hosting network
- **Green "Network: Active (Connected to SSID)"**: Connected to existing network

## Robot Management

### Robot Discovery
**Purpose**: Find and connect to available robots on the network

1. **Ensure Network is Active**: Green network status required
2. **Click "Discover Robots"**: Initiates broadcast discovery
3. **Wait for Results**: Robots will appear in the table
4. **Automatic Updates**: Table refreshes with robot status

### Robot Configuration via Bluetooth

#### Initial Setup (New Robots)
1. **Enable Bluetooth** on your device (phone/computer)
2. **Scan for Devices**: Look for "SoccerBot_ROBOT_XXX"
3. **Connect**: No pairing code required
4. **Send WiFi Configuration**:
   ```json
   {
     "command": "configure_wifi",
     "ssid": "Your_Network_Name",
     "password": "your_network_password"
   }
   ```
5. **Robot Restarts**: Automatically connects to configured network

#### Using the GUI Configuration
1. **Select Robot** in the robot table
2. **Click "Configure WiFi"**
3. **Enter Network Details** in the dialog
4. **Click "Configure"**
5. **Wait for Restart**: Robot will reconnect automatically

### Robot Status Information

#### Robot Table Columns
- **Name**: Robot identifier (configurable)
- **IP Address**: Current network address
- **Status**: Connection state
- **Last Seen**: Time since last communication
- **Paired**: Controller pairing status

#### Status Meanings
- **Connected**: Robot is online and responsive
- **Disconnected**: Robot is offline or unresponsive
- **Configuring**: Robot is being configured

### Robot Management Actions
- **Configure WiFi**: Set network credentials via Bluetooth
- **Remove Robot**: Remove robot from the system
- **Refresh**: Manual update of robot status

## Controller Management

### Controller Detection
**Automatic**: Controllers are detected when plugged in
**Supported Types**: Xbox, PlayStation, generic USB controllers

### Controller Status
- **Connected**: Controller is active and responsive
- **Disconnected**: Controller is not responding

### Pairing Controllers with Robots

#### Method 1: Using the GUI
1. **Select Controller** in the controller table
2. **Click "Pair with Robot"**
3. **Choose Robot** from the available list
4. **Confirm Pairing**

#### Method 2: Drag and Drop (if implemented)
1. **Drag Controller** from controller list
2. **Drop on Robot** in robot list
3. **Confirm Pairing**

### Unpairing Controllers
1. **Select Paired Controller**
2. **Click "Unpair"**
3. **Confirm Action**

### Controller Input Mapping

#### Default Controls
- **Left Stick**: Movement (forward/backward, left/right)
- **Right Stick**: Rotation
- **No Movement**: Automatic stop command

#### Input Processing
- **Deadzone**: Small movements ignored to prevent drift
- **Scaling**: Input values normalized to -1.0 to +1.0 range
- **Rate Limiting**: Commands sent at 60Hz for smooth control

## System Status

### Status Bar Information
- **Network Status**: Current network connection state
- **Robot Count**: Number of connected robots
- **Controller Count**: Number of active controllers
- **Timestamp**: Current system time

### LED Status Indicators (ESP32 Robots)

#### Built-in LED (GPIO 2) - System Status
- **Solid On**: Robot connected and ready
- **Blinking**: Robot in configuration or error state
- **Off**: Robot offline or starting up

#### WiFi LED (GPIO 4)
- **On**: WiFi connected
- **Off**: WiFi disconnected

#### Bluetooth LED (GPIO 5)
- **On**: Bluetooth enabled and available
- **Off**: Bluetooth disabled

#### Command LED (GPIO 18)
- **Brief Flash**: Command received and processed
- **Off**: No recent commands

## Advanced Features

### Multiple Robot Control
- **Team Coordination**: Control multiple robots simultaneously
- **Individual Pairing**: Each controller pairs with one robot
- **Status Monitoring**: Individual status for each robot
- **Synchronized Commands**: Commands sent to all paired robots

### Network Optimization
- **Low Latency Mode**: Optimized settings for competitive use
- **Bandwidth Management**: Efficient use of network resources
- **Error Recovery**: Automatic reconnection and recovery

### Logging and Diagnostics
- **Console Output**: Detailed logging in application console
- **Network Monitoring**: Real-time network performance data
- **Error Reporting**: Comprehensive error messages and solutions

### Configuration Persistence
- **Robot Settings**: WiFi credentials saved on robot
- **Application Preferences**: GUI settings saved between sessions
- **Pairing Memory**: Controller-robot pairs remembered

## Troubleshooting

### Common Issues and Solutions

#### "No Robots Discovered"
**Causes**: Network issues, robot configuration, firewall
**Solutions**:
1. Verify network connection (green status)
2. Check robot power and LED status
3. Ensure robots are on same network
4. Disable firewall temporarily
5. Restart discovery process

#### "Controller Not Detected"
**Causes**: Driver issues, USB connection, compatibility
**Solutions**:
1. Check USB cable connection
2. Install controller drivers
3. Try different USB port
4. Restart application
5. Use different controller

#### "High Input Latency"
**Causes**: Network congestion, interference, processing load
**Solutions**:
1. Use dedicated network for robots
2. Reduce WiFi interference
3. Close unnecessary applications
4. Check robot power supply
5. Optimize network settings

#### "Robot Disconnects Frequently"
**Causes**: Power issues, network instability, interference
**Solutions**:
1. Check robot power supply
2. Reduce WiFi interference sources
3. Move closer to access point
4. Check network stability
5. Update robot firmware

#### "Cannot Pair Controller"
**Causes**: Robot not discovered, already paired, communication error
**Solutions**:
1. Ensure robot is discovered and connected
2. Unpair from previous robot if needed
3. Restart pairing process
4. Check robot and controller status
5. Restart application if needed

### Diagnostic Steps

#### Network Diagnostics
1. **Check Network Status**: Verify green network indicator
2. **Test Connectivity**: Ping robot IP addresses
3. **Monitor Traffic**: Check UDP port activity
4. **Firewall Check**: Verify ports 12345, 12346 are open

#### Robot Diagnostics
1. **LED Status**: Check all four LED indicators
2. **Serial Monitor**: View ESP32 debug output
3. **Bluetooth Test**: Connect and send status command
4. **Power Check**: Verify stable 5V supply

#### Controller Diagnostics
1. **Device Manager**: Check if controller appears
2. **Test Software**: Use controller testing application
3. **Driver Update**: Install latest controller drivers
4. **Cable Check**: Try different USB cable

## Best Practices

### Setup Recommendations
1. **Dedicated Network**: Use separate network for robots when possible
2. **Stable Power**: Ensure reliable power supplies for all robots
3. **Minimal Interference**: Keep away from other 2.4GHz devices
4. **Backup Equipment**: Have spare controllers and robots ready
5. **Pre-configured Robots**: Set up all robots before events

### Operational Guidelines
1. **Regular Discovery**: Periodically rediscover robots
2. **Monitor Status**: Watch status indicators during operation
3. **Graceful Shutdown**: Stop robots before shutting down system
4. **Network Priority**: Give robot network priority over other traffic
5. **Firmware Updates**: Keep robot firmware updated

### Performance Optimization
1. **Close Unnecessary Apps**: Minimize system load
2. **Quality Controllers**: Use high-quality gaming controllers
3. **Short Cables**: Use short, high-quality USB cables
4. **Network Isolation**: Separate robot traffic from other data
5. **Regular Maintenance**: Keep systems updated and maintained

### Tournament Setup
1. **Early Setup**: Configure systems well before events
2. **Backup Plans**: Have alternative controllers and networks ready
3. **Team Coordination**: Establish clear setup procedures
4. **Testing Phase**: Thoroughly test all systems before competition
5. **Quick Recovery**: Practice rapid system recovery procedures

---

For technical support or additional questions, refer to the Installation Guide or contact the development team.
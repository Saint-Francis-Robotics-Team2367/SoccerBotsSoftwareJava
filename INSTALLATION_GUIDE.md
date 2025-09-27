# SoccerBots Robotics Control System - Installation Guide

## System Requirements

### Host Computer (Java Application)
- **Operating System**: Windows 10/11, macOS 10.14+, or Linux Ubuntu 18.04+
- **Java**: Java 11 or later (OpenJDK or Oracle JDK)
- **RAM**: Minimum 4GB, Recommended 8GB+
- **Storage**: 500MB free space
- **Network**: WiFi adapter with hosted network capability (Windows) or WiFi access point capability
- **Controllers**: USB game controllers (Xbox, PlayStation, etc.)

### ESP32 Robots
- **Hardware**: ESP32-WROOM development boards
- **Power**: 5V power supply or battery pack
- **LEDs**: 4x status LEDs with 220Ω resistors
- **Arduino IDE**: Version 1.8.19 or later, or Arduino IDE 2.0+

## Installation Steps

### 1. Java Host Application Setup

#### Prerequisites
1. **Install Java 11+**
   ```bash
   # Check if Java is installed
   java -version
   
   # If not installed, download from:
   # https://adoptium.net/ (recommended)
   # or https://www.oracle.com/java/technologies/downloads/
   ```

2. **Install Maven (if building from source)**
   ```bash
   # Download from: https://maven.apache.org/download.cgi
   # Or use package manager:
   
   # Windows (using Chocolatey)
   choco install maven
   
   # macOS (using Homebrew)
   brew install maven
   
   # Ubuntu/Debian
   sudo apt install maven
   ```

#### Build and Run
1. **Clone or download the project**
   ```bash
   git clone <repository-url>
   cd SoccerBotsSoftwareJava
   ```

2. **Build the application**
   ```bash
   mvn clean compile assembly:single
   ```

3. **Run the application**
   ```bash
   # Using Maven
   mvn exec:java -Dexec.mainClass="com.soccerbots.control.RoboticsControlApp"
   
   # Or using the JAR file
   java -jar target/robotics-control-system-1.0.0-jar-with-dependencies.jar
   ```

### 2. ESP32 Robot Setup

#### Arduino IDE Configuration
1. **Install Arduino IDE**
   - Download from: https://www.arduino.cc/en/software
   - Install version 1.8.19+ or Arduino IDE 2.0+

2. **Add ESP32 Board Support**
   - Open Arduino IDE
   - Go to **File > Preferences**
   - Add this URL to "Additional Board Manager URLs":
     ```
     https://dl.espressif.com/dl/package_esp32_index.json
     ```
   - Go to **Tools > Board > Board Manager**
   - Search for "esp32" and install "esp32 by Espressif Systems"

3. **Install Required Libraries**
   - Go to **Sketch > Include Library > Manage Libraries**
   - Search and install:
     - **ArduinoJson** by Benoit Blanchon (v6.21.3+)

#### Hardware Setup
1. **LED Connections**
   ```
   ESP32 Pin  -> Component
   GPIO 2     -> Built-in LED (status)
   GPIO 4     -> WiFi LED + 220Ω resistor -> GND
   GPIO 5     -> Bluetooth LED + 220Ω resistor -> GND
   GPIO 18    -> Command LED + 220Ω resistor -> GND
   3.3V       -> LED anodes (through resistors)
   ```

2. **Power Supply**
   - Connect 5V power supply to VIN and GND
   - Or use 3.3V supply to 3V3 and GND
   - Ensure stable power for reliable WiFi operation

#### Firmware Upload
1. **Configure Robot Settings**
   - Open `esp32_robot_code/soccerbot_main/soccerbot_main.ino`
   - Modify these constants for each robot:
     ```cpp
     const char* ROBOT_TEAM_NAME = "Your_Team_Name";
     const char* ROBOT_DEFAULT_NAME = "SoccerBot_01"; // Unique for each robot
     const char* ROBOT_ID = "ROBOT_001";              // Unique ID
     ```

2. **Upload Firmware**
   - Connect ESP32 to computer via USB
   - Select **Tools > Board > ESP32 Dev Module**
   - Select the correct **Port**
   - Set **Upload Speed** to 921600
   - Click **Upload** button

3. **Verify Installation**
   - Open Serial Monitor (115200 baud)
   - Reset ESP32
   - You should see initialization messages

### 3. Network Configuration

#### Option A: Host Your Own Network (Recommended)
1. **Windows Setup**
   - Ensure you have administrator privileges
   - Run the Java application as administrator
   - Use the "Host Own Network" option in the GUI
   - Default SSID: "SoccerBots_Network"
   - Default Password: "soccerbots123"

2. **Linux/macOS Setup**
   - Install hostapd and dnsmasq:
     ```bash
     # Ubuntu/Debian
     sudo apt install hostapd dnsmasq
     
     # macOS
     brew install hostapd dnsmasq
     ```
   - Configure as access point (manual setup required)

#### Option B: Connect to Existing Network
1. **Network Requirements**
   - 2.4GHz WiFi network (ESP32 limitation)
   - Same network for host computer and robots
   - UDP multicast support (most home networks)

2. **Router Configuration**
   - Ensure UDP ports 12345 and 12346 are not blocked
   - Disable AP isolation if present
   - Enable multicast/broadcast forwarding

### 4. Initial Configuration

#### Configure Robots via Bluetooth
1. **Pair with Robot**
   - Enable Bluetooth on your phone/computer
   - Look for "SoccerBot_ROBOT_XXX" devices
   - Connect (no pairing code required)

2. **Send WiFi Configuration**
   - Use a Bluetooth terminal app or the GUI
   - Send JSON configuration:
     ```json
     {
       "command": "configure_wifi",
       "ssid": "Your_Network_Name",
       "password": "your_password"
     }
     ```

3. **Verify Connection**
   - Robot will restart and connect to WiFi
   - LEDs should indicate successful connection
   - Robot should appear in discovery

#### Test the System
1. **Start the Java Application**
   - Launch the GUI application
   - Configure network (host or connect)
   - Click "Discover Robots"

2. **Connect Controllers**
   - Plug in USB game controllers
   - They should appear in the Controller panel
   - Pair controllers with discovered robots

3. **Test Movement**
   - Use controller sticks to send movement commands
   - Verify commands are received (LED indicators)
   - Check latency and responsiveness

## Troubleshooting

### Java Application Issues
- **"No Java found"**: Install Java 11+ and add to PATH
- **"Controller not detected"**: Install controller drivers
- **"Network failed"**: Run as administrator (Windows)
- **"Build failed"**: Ensure Maven is installed and configured

### ESP32 Issues
- **"Upload failed"**: Check USB cable, drivers, and board selection
- **"WiFi connection failed"**: Verify 2.4GHz network and credentials
- **"Bluetooth not working"**: Ensure ESP32 has Bluetooth enabled
- **"Discovery not working"**: Check network connectivity and firewall

### Network Issues
- **"Robots not discovered"**: Check same network and UDP ports
- **"High latency"**: Use 5GHz host network, reduce interference
- **"Intermittent connection"**: Check power supply stability
- **"Can't host network"**: Ensure admin privileges and compatible adapter

### Controller Issues
- **"Controller not recognized"**: Install drivers or try different controller
- **"Input lag"**: Update controller drivers, check USB connection
- **"Pairing failed"**: Ensure robot is discovered and connected

## Performance Optimization

### Low Latency Tips
1. **Use 5GHz WiFi** for the host computer
2. **Dedicated 2.4GHz network** for robots only
3. **Reduce controller polling interval** in code if needed
4. **Stable power supply** for ESP32 boards
5. **Minimal network congestion** in robot frequency band

### System Scaling
- **Multiple robots**: Ensure adequate network bandwidth
- **Large teams**: Consider multiple access points
- **Tournament setup**: Use dedicated network infrastructure
- **Backup systems**: Have spare controllers and robots ready

## Support and Updates

- **Documentation**: Check README.md for latest information
- **Issues**: Report bugs on the project repository
- **Updates**: Check for firmware and software updates regularly
- **Community**: Join the SoccerBots community for support

---

For additional help, consult the User Manual or contact the development team.
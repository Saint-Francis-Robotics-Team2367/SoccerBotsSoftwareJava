# ESP32 Minibot Firmware

This firmware enables ESP32-based robots to communicate with the SoccerBots control system via WiFi/UDP.

## Setup Instructions

### 1. Install Arduino IDE
- Download and install [Arduino IDE 2.x](https://www.arduino.cc/en/software)

### 2. Install ESP32 Board Support
1. Open Arduino IDE
2. Go to **File → Preferences**
3. Add this URL to "Additional Board Manager URLs":
   ```
   https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
   ```
4. Go to **Tools → Board → Boards Manager**
5. Search for "esp32"
6. Install **"esp32" by Espressif Systems**
   - **IMPORTANT**: Use version **2.0.17** (NOT 3.x if you get compile errors)
   - Version 3.x has known issues with nested includes

### 3. Configure Robot
1. Open `minibots.ino` in Arduino IDE
2. Change the robot name on line 5:
   ```cpp
   Minibot bot("YOUR NAME HERE");  // ← Change this to your robot's name
   ```
3. Verify WiFi credentials in `minibot.h` match your network:
   ```cpp
   #define WIFI_SSID "WATCHTOWER"
   #define WIFI_PASSWORD "lancerrobotics"
   ```

### 4. Select Board and Port
1. Go to **Tools → Board** → Select "ESP32 Dev Module" or your specific ESP32 board
2. Go to **Tools → Port** → Select the COM port for your ESP32

### 5. Upload Firmware
1. Click the **Upload** button (→)
2. Wait for compilation and upload to complete
3. Open **Serial Monitor** (115200 baud) to see connection status

## Troubleshooting

### Error: "nested #include depth 200 exceeds maximum of 200"

This error occurs with ESP32 Arduino Core 3.x. Here are the fixes:

**Option 1: Downgrade ESP32 Core (Recommended)**
1. Go to **Tools → Board → Boards Manager**
2. Search for "esp32"
3. Click on "esp32 by Espressif Systems"
4. Select version **2.0.17** from the dropdown
5. Click "Install"
6. Restart Arduino IDE
7. Try compiling again

**Option 2: Clear Arduino Cache**
1. Close Arduino IDE completely
2. Delete the Arduino cache folder:
   - Windows: `C:\Users\<YourName>\AppData\Local\Arduino15\`
   - macOS: `~/Library/Arduino15/`
   - Linux: `~/.arduino15/`
3. Restart Arduino IDE
4. Reinstall ESP32 board support (version 2.0.17)

**Option 3: Fresh Arduino IDE Install**
1. Uninstall Arduino IDE
2. Delete all Arduino folders:
   - Windows:
     - `C:\Users\<YourName>\AppData\Local\Arduino15\`
     - `C:\Users\<YourName>\Documents\Arduino\`
   - macOS:
     - `~/Library/Arduino15/`
     - `~/Documents/Arduino/`
3. Reinstall Arduino IDE
4. Install ESP32 core version 2.0.17

**Option 4: Use Platform IO (Alternative)**
If Arduino IDE continues to have issues, consider using [PlatformIO](https://platformio.org/) instead.

### Robot Not Connecting to WiFi

1. Check WiFi credentials in `minibot.h`
2. Ensure WiFi network is 2.4GHz (ESP32 doesn't support 5GHz)
3. Check Serial Monitor (115200 baud) for connection messages
4. Verify ESP32 is powered properly

### Robot Not Appearing in Control System

1. Ensure robot and control system are on the same network
2. Check that firewall allows UDP on ports 12345 and 2367
3. Verify robot name matches in both firmware and control system
4. Check Serial Monitor for "Sent discovery ping" messages

### Motor Issues

1. Verify motor pins in constructor match your hardware:
   ```cpp
   Minibot bot("RobotName", leftPin, rightPin, dcPin, servoPin);
   ```
2. Check PWM frequency and resolution settings
3. Ensure motors are properly connected to ESP32 GPIO pins

## Pin Configuration

Default motor pin assignments:
- **Left Motor**: GPIO 16
- **Right Motor**: GPIO 17
- **DC Motor**: GPIO 18
- **Servo Motor**: GPIO 19

To change pins, modify the constructor in `minibots.ino`:
```cpp
Minibot bot("RobotName", 16, 17, 18, 19);  // left, right, dc, servo
```

## Network Protocol

- **Discovery**: Robot broadcasts UDP to 255.255.255.255:12345 every 2 seconds
- **Commands**: Control system sends to robot IP on port 2367
- **Format**: Binary packets (24 bytes) containing joystick and button data

## LED Status Indicators

- **Connecting to WiFi**: Built-in LED blinks
- **Connected**: LED stays on
- **Emergency Stop**: Motors stop, LED blinks rapidly

## Support

For issues not covered here, check the main project README or contact the development team.

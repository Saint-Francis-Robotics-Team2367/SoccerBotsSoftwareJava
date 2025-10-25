# Controller Detection Troubleshooting Guide

## Issue: Controllers Not Showing Up

If your PS5 DualSense or other USB controllers are not appearing in the Controllers panel, follow these debugging steps:

### Step 1: Check Backend Logs

When you run `npm run dev`, look for these log messages in the console:

#### Good Signs ✅
```
Scanning X total input devices from JInput
Detected new controller: Wireless Controller (Type: ps5)
Controller refresh completed. Found X controllers
```

#### Bad Signs ❌
```
JInput native libraries not available
Controller environment is not available
No controllers array returned from environment (NULL)
```

### Step 2: Test Controller Detection Manually

1. Start the application: `npm run dev`
2. Wait for the Electron window to open
3. Click the **Refresh button** (↻) in the Controllers panel
4. Watch the backend console output for detailed logs

You should see:
```
========== MANUAL CONTROLLER REFRESH REQUESTED ==========
Refreshing controller environment...
Controller environment class: net.java.games.input.DefaultControllerEnvironment
Current controller count before refresh: 0
Scanning X total input devices from JInput
========== REFRESH COMPLETED: Found X controllers ==========
  - Wireless Controller (Type: ps5, ID: Wireless_Controller_12345)
```

### Step 3: Check JInput Native Libraries

#### Windows
The error "JInput native libraries not available" means the DLLs aren't loading.

**Solution 1: Check native folder**
```bash
ls native/
```
Should contain:
- `jinput-dx8_64.dll`
- `jinput-raw_64.dll`

**Solution 2: Copy to system path**
If the `native` folder is missing or incomplete:

1. Download JInput binaries from https://github.com/jinput/jinput/releases
2. Extract the native DLLs
3. Copy them to `C:\Users\r2d2d\Documents\Dev\Robotics\SoccerBotsSoftwareJava\native\`

**Solution 3: Run with explicit library path**
```bash
mvn exec:java -Dexec.mainClass="com.soccerbots.control.HeadlessLauncher" -Djava.library.path="./native"
```

#### macOS
```bash
ls native/
```
Should contain:
- `libjinput-osx.dylib`

#### Linux
```bash
ls native/
```
Should contain:
- `libjinput-linux64.so`

### Step 4: Verify Controller Connection

#### Windows Device Manager
1. Open Device Manager (Win + X → Device Manager)
2. Look for your controller under:
   - **Human Interface Devices** (HID devices)
   - **Sound, video and game controllers**
   - **Xbox Peripherals** (for Xbox controllers)

#### PS5 DualSense Specific
The PS5 DualSense controller may show up as:
- "Wireless Controller"
- "DualSense Wireless Controller"
- "PS5 Controller"
- Generic "HID-compliant game controller"

**USB vs Bluetooth**: For best compatibility, connect via **USB cable** first. Bluetooth may require additional drivers.

### Step 5: Test with Simple Java Program

Create `TestController.java`:
```java
import net.java.games.input.*;

public class TestController {
    public static void main(String[] args) {
        ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
        Controller[] controllers = env.getControllers();

        System.out.println("Found " + controllers.length + " controllers:");
        for (Controller controller : controllers) {
            System.out.println("- " + controller.getName() + " (Type: " + controller.getType() + ")");
        }
    }
}
```

Compile and run:
```bash
javac -cp "target/robotics-control-system-1.0.0-jar-with-dependencies.jar" TestController.java
java -Djava.library.path="./native" -cp "target/robotics-control-system-1.0.0-jar-with-dependencies.jar:." TestController
```

### Step 6: Common Issues and Fixes

#### Issue: "Found 0 controllers" but controller is plugged in
**Cause**: Windows may not recognize the controller properly
**Fix**:
1. Unplug the controller
2. Plug it back in
3. Wait 5 seconds
4. Click Refresh in the UI
5. Check Windows Game Controllers: `joy.cpl` in Run dialog

#### Issue: Controller shows in Windows but not in app
**Cause**: JInput filter is too strict
**Fix**: Check the detection patterns in `ControllerManager.java`:
```java
// Line 78-91
if (controller.getType() == Controller.Type.GAMEPAD ||
    controller.getType() == Controller.Type.STICK ||
    // ... etc
```

#### Issue: "UnsatisfiedLinkError: no jinput-dx8_64"
**Cause**: Native DLLs not in library path
**Fix**:
1. Create `native` folder in project root
2. Download JInput native libraries
3. Place DLLs in `native` folder
4. Restart application

### Step 7: Enable Debug Logging

Edit `src/main/resources/logback.xml`:
```xml
<logger name="net.java.games.input" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
</logger>

<logger name="com.soccerbots.control.controller" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
</logger>
```

Rebuild and run:
```bash
mvn clean compile assembly:single
npm run dev
```

### Step 8: Check Controller in Browser DevTools

1. Open the Electron app
2. Press Ctrl+Shift+I (DevTools)
3. Go to Console tab
4. Type: `navigator.getGamepads()`
5. Should show an array of connected gamepads

**Note**: Electron uses the browser's Gamepad API, but our backend uses JInput (native Java). They're independent.

### Step 9: Alternative: Use DirectInput Test

Windows includes a built-in controller tester:
1. Press Win + R
2. Type: `joy.cpl`
3. Press Enter
4. Select your controller
5. Click "Properties"
6. Test buttons and axes

If the controller works here but not in the app, it's a JInput configuration issue.

### Step 10: Force Controller Types

If detection patterns aren't working, you can force add controllers by type:

In `ControllerManager.java`, modify the condition to be more permissive:
```java
// Line 78 - Make this TRUE to see ALL detected devices
if (true) {  // Temporarily accept ALL controllers for debugging
    logger.info("DETECTED DEVICE: {} (Type: {})", controller.getName(), controller.getType());
    // ... rest of code
}
```

This will show you exactly what JInput is detecting.

## PS5 DualSense Specific Troubleshooting

### Driver Issues
PS5 DualSense controllers may need:
1. **DS4Windows** (for Windows) - Makes PS5 controllers appear as Xbox controllers
   - Download: https://github.com/Ryochan7/DS4Windows/releases
   - Install and run
   - Should auto-detect PS5 controller

2. **Steam Input** - If you have Steam installed, it may interfere
   - Open Steam → Settings → Controller → General Controller Settings
   - Uncheck "PlayStation Configuration Support"

### USB Cable
- Use the **official** USB-C cable that came with the PS5 controller
- Some charging cables are "charge-only" and don't carry data

### Windows Game Services
Make sure Windows recognizes the controller:
```powershell
# PowerShell (Run as Administrator)
Get-PnpDevice -Class HIDClass
```

Should show PS5 controller as a HID device.

## Still Not Working?

### Check These Files
1. `pom.xml` - Ensure JInput dependency is correct
2. `native/` folder - Ensure native libraries exist
3. Windows antivirus - May block DLL loading

### Report the Issue
If controllers still don't show up, run the app and send us:

1. Full console output (backend logs)
2. Output from `joy.cpl` (Windows)
3. Output from `navigator.getGamepads()` (DevTools)
4. Controller model and connection type (USB/Bluetooth)

---

## Quick Reference

### Logs to Look For
```
✅ "Scanning X total input devices from JInput"
✅ "Detected new controller: [name]"
❌ "JInput native libraries not available"
❌ "Controller environment is not available"
```

### Commands
```bash
# Rebuild
mvn clean compile assembly:single

# Run
npm run dev

# Test native libs
ls native/

# Windows controller test
joy.cpl
```

### Important Files
- `src/main/java/com/soccerbots/control/controller/ControllerManager.java` - Detection logic
- `native/` - Native JInput DLLs
- `pom.xml` - JInput dependency
- `src/main/resources/logback.xml` - Logging configuration

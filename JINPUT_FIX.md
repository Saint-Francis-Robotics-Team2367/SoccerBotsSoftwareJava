# JInput Native Library Fix

## Problem
The log shows: `Scanning 0 total input devices from JInput`

This means JInput's native libraries (DirectInput on Windows) are not detecting any controllers, even though they may be plugged in.

## Root Cause
JInput relies on Windows DirectInput API, which requires native DLL files that may be:
1. Missing from the project
2. Not properly loaded by Java
3. Blocked by Windows security
4. Incompatible with your Windows version

## Solutions (Try in Order)

### Solution 1: Install Native Libraries (RECOMMENDED)

1. **Download JInput Native Libraries**
   ```bash
   # Create native directory if it doesn't exist
   mkdir -p native
   ```

2. **Get the DLLs** from Maven repository:
   - Go to: https://repo1.maven.org/maven2/net/java/jinput/jinput-platform/2.0.9/
   - Download: `jinput-platform-2.0.9-natives-windows.jar`

3. **Extract the DLLs**:
   ```bash
   cd native
   # Extract the jar file (it's just a zip)
   jar xf jinput-platform-2.0.9-natives-windows.jar
   ```

   Or manually extract these files to the `native/` folder:
   - `jinput-dx8_64.dll`
   - `jinput-raw_64.dll`
   - `jinput-wintab.dll` (optional)

4. **Verify the files**:
   ```bash
   ls native/
   ```

   Should show:
   ```
   jinput-dx8_64.dll
   jinput-raw_64.dll
   ```

5. **Restart the application**:
   ```bash
   npm run dev
   ```

### Solution 2: Use Alternative JInput Version

Edit `pom.xml` and try JInput version 2.0.10:

```xml
<dependency>
    <groupId>net.java.games</groupId>
    <artifactId>jinput</artifactId>
    <version>2.0.10</version>
</dependency>
<dependency>
    <groupId>net.java.games</groupId>
    <artifactId>jinput-platform</artifactId>
    <version>2.0.10</version>
    <classifier>natives-all</classifier>
</dependency>
```

Then rebuild:
```bash
mvn clean compile assembly:single
npm run dev
```

### Solution 3: Use JInput from Different Repository

Some users have success with this alternative JInput:

Add to `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.strikerx3</groupId>
    <artifactId>jinput</artifactId>
    <version>2.0.9</version>
</dependency>
```

### Solution 4: Use SDL2 Instead of JInput

SDL2 has better controller support. We can add it as an alternative.

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.libsdl</groupId>
    <artifactId>sdl2gdx</artifactId>
    <version>1.0.4</version>
</dependency>
```

### Solution 5: Bypass JInput and Use Web Gamepad API

Since the app runs in Electron, we can use the browser's Gamepad API instead of JInput.

Create a new WebSocket message type for controller input from the frontend.

## Quick Test Script

Create `TestJInput.java` in project root:

```java
import net.java.games.input.*;

public class TestJInput {
    public static void main(String[] args) {
        System.out.println("Testing JInput...");
        System.out.println("Java library path: " + System.getProperty("java.library.path"));

        try {
            ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
            System.out.println("Environment: " + env.getClass().getName());

            Controller[] controllers = env.getControllers();
            System.out.println("Found " + controllers.length + " controllers");

            for (int i = 0; i < controllers.length; i++) {
                Controller c = controllers[i];
                System.out.println("\nController " + i + ":");
                System.out.println("  Name: " + c.getName());
                System.out.println("  Type: " + c.getType());

                Component[] components = c.getComponents();
                System.out.println("  Components: " + components.length);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

Run it:
```bash
# Compile
javac -cp "target/robotics-control-system-1.0.0-jar-with-dependencies.jar" TestJInput.java

# Run with native library path
java -Djava.library.path="./native" -cp "target/robotics-control-system-1.0.0-jar-with-dependencies.jar;." TestJInput
```

## Expected Output

### If Working ✅
```
Testing JInput...
Environment: net.java.games.input.DefaultControllerEnvironment
Found 15 controllers

Controller 0:
  Name: Keyboard
  Type: Keyboard
  Components: 104

Controller 5:
  Name: Wireless Controller
  Type: Gamepad
  Components: 18
```

### If Not Working ❌
```
Testing JInput...
Found 0 controllers
```

## Alternative: Use Electron's Gamepad API

Since JInput is problematic, we can use the browser's built-in Gamepad API:

### Frontend (TypeScript):
```typescript
// Poll gamepads from browser
function pollGamepads() {
  const gamepads = navigator.getGamepads();
  for (let i = 0; i < gamepads.length; i++) {
    const gamepad = gamepads[i];
    if (gamepad) {
      // Send to backend via WebSocket
      apiService.sendControllerInput({
        id: gamepad.id,
        axes: Array.from(gamepad.axes),
        buttons: gamepad.buttons.map(b => b.pressed)
      });
    }
  }
}

// Start polling
setInterval(pollGamepads, 16); // 60 FPS
```

This would work immediately without any native library issues!

## Recommended Action

**Try Solution 1 first** (install native DLLs).

If that doesn't work after trying all solutions, **use Electron's Gamepad API** instead (most reliable for Electron apps).

Would you like me to implement the Electron Gamepad API solution?

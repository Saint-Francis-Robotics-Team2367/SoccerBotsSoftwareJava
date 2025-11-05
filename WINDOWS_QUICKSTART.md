# Windows Quick Start Guide

## 🚀 Quick Launch (Easiest Method)

### First Time Setup
1. **Install Prerequisites:**
   - Python 3.8+ from https://www.python.org/ (Check "Add to PATH" during installation)
   - Node.js 18+ from https://nodejs.org/ (LTS version recommended)

2. **Double-click:** `START.bat`
   - This will install all dependencies and launch the application
   - A window will open with the robot control interface

### Every Time After
- Just double-click `START.bat` to launch the application

---

## 📝 Available Launch Scripts

### `START.bat` - **Recommended for most users**
- Simplest way to run the application
- Automatically installs required tools (concurrently, wait-on)
- Runs everything in development mode with hot-reload
- Opens Electron window automatically

### `launch-dev.bat` - Development Mode
- Full development mode with detailed checks
- Verifies all dependencies are installed
- Shows detailed status messages
- Best for developers and troubleshooting

### `launch-production.bat` - Production Mode
- Builds the frontend for production
- Runs optimized version
- Use this for best performance
- Takes longer to start (build step required)

---

## 🎮 Using the Application

### 1. Connect ESP32 Robots
- Power on your ESP32 robots
- They will automatically appear in the "Robot Connections" panel
- Click "Connect" to establish connection

### 2. Connect Controllers
- Plug in a USB game controller (PlayStation, Xbox, etc.)
- Controller will appear in the "Controllers" panel
- Click "Pair" and select a robot from the list
- Controller is now linked to that robot

### 3. Control Robots
- Use the left stick for movement
- Use the right stick for rotation
- Emergency Stop button stops all robots immediately
- Click Emergency Stop again to resume operations

---

## 🔧 Troubleshooting

### "Python is not installed"
- Download and install Python from https://www.python.org/
- During installation, check "Add Python to PATH"
- Restart your computer after installation
- Run `START.bat` again

### "Node.js is not installed"
- Download and install Node.js from https://nodejs.org/
- Choose the LTS (Long Term Support) version
- Restart your computer after installation
- Run `START.bat` again

### "concurrently command not found"
- Open Command Prompt as Administrator
- Run: `npm install -g concurrently wait-on`
- Close and reopen Command Prompt
- Run `START.bat` again

### Controllers not detected
- Ensure controller is plugged in before starting
- Windows should automatically install drivers
- Try unplugging and replugging the controller
- Click "Refresh" button in Controllers panel

### Robots not discovered
- Ensure robot and computer are on same WiFi network
- Default network: "WATCHTOWER" (password: "lancerrobotics")
- Check robot serial monitor shows "Connected! IP: X.X.X.X"
- Windows Firewall may block UDP port 12345 - allow it
- Click "Refresh" button in Robot Connections panel

### Port 8080 already in use
- Close any other applications using port 8080
- Or kill the Python process:
  - Open Task Manager (Ctrl+Shift+Esc)
  - Find "python.exe" or "Python"
  - End the task
- Run `START.bat` again

### Black screen in Electron window
- Press Ctrl+Shift+I to open DevTools
- Check Console for errors
- Ensure backend started successfully (check terminal)
- Wait 10-15 seconds for everything to initialize

---

## 📦 Building Installer (Advanced)

To create a Windows installer (.exe):

```batch
npm run build:all
cd electron
npm run build:win
```

The installer will be created in `electron/dist/`

---

## 🆘 Getting Help

If you encounter issues:

1. Check the terminal output for error messages
2. Open DevTools in Electron (Ctrl+Shift+I)
3. Check the "Terminal Monitor" panel in the app
4. Review the troubleshooting section above
5. Check the main README.md for detailed documentation

---

## ⚙️ System Requirements

- **OS:** Windows 10/11 (64-bit)
- **RAM:** 4GB minimum, 8GB recommended
- **Disk:** 500MB free space
- **Network:** WiFi adapter (for robot communication)
- **Controllers:** USB game controllers (PS4/PS5/Xbox)

---

## 🔄 Updating the Application

To get the latest version:

```batch
git pull
npm run install:all
```

Then run `START.bat` as usual.

# SoccerBots Control System - Quick Start Guide

## One-Command Development Setup

### Prerequisites
- Python 3.8+ installed
- Node.js 18+ installed
- pip (Python package manager)

### First Time Setup

**IMPORTANT:** Before running the application for the first time, install all dependencies:

```bash
npm run install:all
```

This installs dependencies for:
- Frontend (React + Vite)
- Electron app
- Python backend (Flask, pygame, etc.)

### Run the Application (Single Command!)

```bash
npm run dev
```

This single command will:
1. ✅ Start the Python backend (API server on port 8080)
2. ✅ Start the React frontend dev server (Vite on port 5173)
3. ✅ Wait for both to be ready
4. ✅ Open the native Electron desktop window

**That's it!** The application opens automatically in a native desktop window.

### What You'll See

1. **Console output** showing:
   - `[Python Backend]` - Backend logs
   - `[Frontend]` - Vite dev server
   - `[Electron]` - Electron startup

2. **Native Desktop Window** opens showing the robot control interface

3. **Features Available**:
   - Robot discovery and connection
   - Emergency stop button
   - Match timer (2 minutes default)
   - Network analysis
   - Real-time terminal monitoring
   - Service logs

### Production Build

Build everything and run:
```bash
npm start
```

Create distributable package:
```bash
npm run dist
```

## How It Works

```
npm run dev
    ↓
Starts 3 processes concurrently:
    ├─ Java Backend (mvn exec:java)
    ├─ Frontend Dev Server (vite)
    └─ Electron (waits for both, then opens window)
```

## Manual Control (Advanced)

If you need to run components separately:

**Terminal 1 - Backend:**
```bash
npm run dev:backend
```

**Terminal 2 - Frontend:**
```bash
npm run dev:frontend
```

**Terminal 3 - Electron:**
```bash
npm run dev:electron
```

## Troubleshooting

### Port Already in Use
If port 8080 or 5173 is in use:
1. Stop the conflicting process
2. Or modify ports in `electron/main.js` and `HeadlessLauncher.java`

### Java Backend Won't Start
- Ensure Java 11+ is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Build the backend first: `npm run build:backend`

### Electron Window Doesn't Open
- Check that both backend (8080) and frontend (5173) are running
- Look for error messages in the console
- Try running `npm run dev:electron` separately to see specific errors

## ESP32 Robot Setup

1. Flash the ESP32 firmware from `esp32_robot_firmware/`
2. Update robot name in `minibots.ino`: `Minibot bot("YOUR NAME HERE");`
3. Ensure robots connect to WiFi network "WATCHTOWER"
4. Robots will auto-discover and appear in the Connection Panel

## Match Timer Usage

1. Click **Play** to start the 2-minute match timer
2. Robots can only move during active match (teleop mode)
3. Click **Pause** to stop
4. Click **Reset** to reset timer to 02:00
5. Timer auto-stops at 00:00

## Emergency Stop

Click the **EMERGENCY STOP** button to immediately halt all robots. Click again to deactivate.

---

**Happy Coding! 🤖**

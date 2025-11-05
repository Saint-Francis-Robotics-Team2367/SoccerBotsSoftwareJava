# Building Windows Installer

This guide explains how to create a Windows installer for the SoccerBots Control Station.

## Prerequisites

1. **Windows 10/11** (64-bit)
2. **Python 3.8+** installed and in PATH
3. **Node.js 18+** installed
4. **Git** installed (optional, for cloning)

## Quick Build

### Option 1: Use build script (Recommended)

```batch
npm run dist
```

This will:
1. Build the frontend (React + Vite)
2. Package the Python backend
3. Create an Electron installer in `electron/dist/`

### Option 2: Manual build steps

```batch
REM Step 1: Install dependencies
npm run install:all

REM Step 2: Build frontend
cd frontend
npm run build
cd ..

REM Step 3: Create Electron installer
cd electron
npm run build:win
cd ..
```

## Output

The installer will be created in:
```
electron/dist/SoccerBots Control Setup X.X.X.exe
```

## Installer Features

The Windows installer (.exe) includes:
- ✅ Python backend (all Python files)
- ✅ Frontend UI (built React app)
- ✅ Electron wrapper
- ✅ Desktop shortcut creation
- ✅ Start menu entry
- ✅ Uninstaller

## Installing on User's Computer

### System Requirements
- Windows 10/11 (64-bit)
- **Python 3.8+ must be installed separately** (installer does NOT include Python)
- 4GB RAM minimum
- 500MB free disk space

### Installation Steps

1. **Install Python first:**
   - Download from https://www.python.org/
   - During installation, CHECK "Add Python to PATH"
   - Restart computer

2. **Install Python dependencies:**
   ```batch
   pip install flask flask-cors flask-socketio pygame netifaces
   ```
   
   Or use requirements.txt:
   ```batch
   pip install -r python_backend\requirements.txt
   ```

3. **Run the installer:**
   - Double-click `SoccerBots Control Setup X.X.X.exe`
   - Follow installation wizard
   - Choose installation directory
   - Wait for installation to complete

4. **Launch the app:**
   - Use desktop shortcut, OR
   - Start Menu → SoccerBots Control, OR
   - Navigate to install directory and run `SoccerBots Control.exe`

## Troubleshooting Build

### "electron-builder not found"
```batch
cd electron
npm install
```

### Frontend build fails
```batch
cd frontend
npm install
npm run build
```

### Python dependencies missing
```batch
cd python_backend
pip install -r requirements.txt
```

### Build output is huge
This is normal. The packaged app includes:
- Electron runtime (~150MB)
- Python backend code
- Frontend assets
- Node modules needed at runtime

## Build for Other Platforms

### macOS
```batch
cd electron
npm run build:mac
```

### Linux
```batch
cd electron
npm run build:linux
```

Note: Cross-platform builds require platform-specific tools and may not work on Windows.

## Advanced: Customizing the Build

### Change App Name
Edit `electron/package.json`:
```json
{
  "build": {
    "productName": "Your Custom Name"
  }
}
```

### Change App Icon
Replace `electron/build/icon.ico` with your custom icon.

### Include Additional Files
Edit `electron/package.json` → `build.extraResources`:
```json
{
  "build": {
    "extraResources": [
      {
        "from": "../your_folder",
        "to": "destination_folder"
      }
    ]
  }
}
```

### Reduce Build Size
- Remove development dependencies
- Use production builds only
- Enable compression in electron-builder config

## Distributing the Installer

### Upload to GitHub Releases
1. Create a new release on GitHub
2. Upload the .exe file
3. Add release notes
4. Users can download and install

### Share Direct Download
1. Upload .exe to file hosting (Google Drive, Dropbox, etc.)
2. Share download link
3. Provide installation instructions (link to WINDOWS_QUICKSTART.md)

## Automatic Updates (Optional)

To enable automatic updates:
1. Set up electron-updater in `electron/main.js`
2. Configure update server
3. Sign the installer (requires code signing certificate)

## Code Signing (Optional but Recommended)

Signed installers are trusted by Windows SmartScreen.

1. Purchase code signing certificate
2. Configure electron-builder:
   ```json
   {
     "win": {
       "certificateFile": "path/to/cert.pfx",
       "certificatePassword": "password"
     }
   }
   ```

Without signing:
- Users will see "Windows protected your PC" warning
- They must click "More info" → "Run anyway"
- This is normal for unsigned software

## Testing the Installer

Before distribution:

1. **Test on clean Windows VM:**
   - Fresh Windows 10/11 installation
   - Install Python first
   - Run installer
   - Verify app launches
   - Test all features

2. **Test installation paths:**
   - Program Files
   - Custom directory
   - User directory

3. **Test uninstaller:**
   - Uninstall from Control Panel
   - Verify all files removed
   - Verify shortcuts removed

## Support

If users encounter issues:
- Direct them to WINDOWS_QUICKSTART.md
- Check they have Python installed
- Verify Python is in PATH
- Check firewall settings (UDP ports 12345, 2367)

---

**Happy Building!** 🚀

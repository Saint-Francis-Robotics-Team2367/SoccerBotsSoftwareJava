@echo off
REM SoccerBots Control Station - Development Launcher
REM This script launches the backend, frontend, and Electron app in development mode

echo.
echo ========================================
echo  SoccerBots Control Station Launcher
echo  Development Mode
echo ========================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo Please install Python 3.8+ from https://www.python.org/
    pause
    exit /b 1
)

REM Check if Node.js is installed
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js 18+ from https://nodejs.org/
    pause
    exit /b 1
)

REM Check if npm is installed
npm --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: npm is not installed or not in PATH
    echo Please install Node.js (which includes npm)
    pause
    exit /b 1
)

echo [OK] Python found: 
python --version
echo [OK] Node.js found:
node --version
echo [OK] npm found:
npm --version
echo.

REM Check if dependencies are installed
echo Checking dependencies...
if not exist "node_modules" (
    echo Installing root dependencies...
    call npm install
    if errorlevel 1 (
        echo ERROR: Failed to install root dependencies
        pause
        exit /b 1
    )
)

if not exist "frontend\node_modules" (
    echo Installing frontend dependencies...
    cd frontend
    call npm install
    if errorlevel 1 (
        echo ERROR: Failed to install frontend dependencies
        pause
        exit /b 1
    )
    cd ..
)

if not exist "electron\node_modules" (
    echo Installing Electron dependencies...
    cd electron
    call npm install
    if errorlevel 1 (
        echo ERROR: Failed to install Electron dependencies
        pause
        exit /b 1
    )
    cd ..
)

echo Checking Python dependencies...
python -c "import flask, flask_cors, flask_socketio, pygame, netifaces" 2>nul
if errorlevel 1 (
    echo Installing Python dependencies...
    cd python_backend
    python -m pip install -r requirements.txt
    if errorlevel 1 (
        echo ERROR: Failed to install Python dependencies
        pause
        exit /b 1
    )
    cd ..
)

echo.
echo [OK] All dependencies installed
echo.
echo ========================================
echo  Starting SoccerBots Control Station
echo ========================================
echo.
echo This will start:
echo  1. Python Backend (port 8080)
echo  2. React Frontend Dev Server (port 5173)
echo  3. Electron Desktop App
echo.
echo Press Ctrl+C to stop all services
echo.

REM Launch the application using npm dev script
call npm run dev

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start application
    echo.
    echo If you see "concurrently command not found", run:
    echo   npm install -g concurrently
    echo.
    pause
    exit /b 1
)

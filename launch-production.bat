@echo off
REM SoccerBots Control Station - Production Launcher
REM This script builds and launches the production Electron app

echo.
echo ========================================
echo  SoccerBots Control Station
echo  Production Build and Launch
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

echo [OK] Python found: 
python --version
echo [OK] Node.js found:
node --version
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
    cd ..
)

if not exist "electron\node_modules" (
    echo Installing Electron dependencies...
    cd electron
    call npm install
    cd ..
)

echo Checking Python dependencies...
python -c "import flask, flask_cors, flask_socketio, pygame, netifaces" 2>nul
if errorlevel 1 (
    echo Installing Python dependencies...
    cd python_backend
    python -m pip install -r requirements.txt
    cd ..
)

echo.
echo [OK] Dependencies ready
echo.
echo ========================================
echo  Building Application
echo ========================================
echo.

REM Build the application
echo Building frontend...
cd frontend
call npm run build
if errorlevel 1 (
    echo ERROR: Frontend build failed
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo [OK] Frontend built successfully
echo.

REM Run the production application
echo ========================================
echo  Starting Application
echo ========================================
echo.

REM Start Python backend in background
echo Starting Python backend...
start /B python python_backend\main.py 8080

REM Wait for backend to start
echo Waiting for backend to start...
timeout /t 3 /nobreak >nul

REM Check if backend is running
curl -s http://localhost:8080/api/health >nul 2>&1
if errorlevel 1 (
    echo WARNING: Backend may not have started properly
    echo Continuing anyway...
)

echo.
echo Starting Electron app...
cd electron
call npm start

REM Cleanup - kill Python backend when Electron closes
echo.
echo Application closed. Cleaning up...
taskkill /F /IM python.exe /FI "WINDOWTITLE eq main.py" >nul 2>&1

echo.
echo Application stopped.
pause

@echo off
REM SoccerBots Control Station - Quick Start
REM Simply runs 'npm run dev' to launch everything

echo.
echo ========================================
echo  SoccerBots Control Station
echo  Quick Start
echo ========================================
echo.

REM Check for concurrently
npm list -g concurrently >nul 2>&1
if errorlevel 1 (
    echo Installing concurrently globally...
    npm install -g concurrently
)

REM Check for wait-on
npm list -g wait-on >nul 2>&1
if errorlevel 1 (
    echo Installing wait-on globally...
    npm install -g wait-on
)

echo.
echo Starting application...
echo.

call npm run dev

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start
    echo.
    echo Try running: npm run install:all
    echo Then run this script again
    echo.
    pause
)

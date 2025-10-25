@echo off
echo ========================================
echo   JInput Native Library Installer
echo ========================================
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0install-jinput-natives.ps1"

echo.
echo Press any key to exit...
pause >nul

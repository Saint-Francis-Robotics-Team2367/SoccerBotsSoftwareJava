# SoccerBots Control Station

## Project Overview

This project is a control system for ESP32-based soccer robots. It consists of a Java backend that provides a REST API and a WebSocket server for real-time communication, and multiple frontend options:

*   **Electron/React App:** A modern desktop application with a React-based UI.
*   **JavaFX GUI:** A traditional desktop GUI.
*   **3D Simulator:** A lightweight simulator for testing without hardware.
*   **Headless API Mode:** Allows for custom frontends to be built on top of the backend.

The project also includes the firmware for the ESP32 robots.

**Technologies:**

*   **Backend:** Java 17, Maven, Javalin (web server), JInput (controller support), Jackson (JSON processing)
*   **Frontend (Electron/React):** Node.js, npm, React, TypeScript, Vite, Electron
*   **Firmware:** C++/Arduino for ESP32

## Building and Running

### Development

The recommended way to run the project for development is to use the following command:

```bash
npm run dev
```

This will start the Java backend, the React frontend, and the Electron app concurrently.

### Production

To build the project for production and create a distributable installer, use the following command:

```bash
npm run dist
```

This will create an installer in the `electron/dist` directory.

### Other Run Options

*   **JavaFX GUI:** `mvn javafx:run`
*   **3D Simulator:** `mvn exec:java -Dexec.mainClass="com.soccerbots.control.simulator.SimulatorApp"`
*   **Headless API Mode:** `mvn exec:java -Dexec.mainClass="com.soccerbots.control.HeadlessLauncher"`

## Development Conventions

*   The Java backend follows standard Maven project structure.
*   The React frontend is built with Vite and uses TypeScript.
*   The Electron app is used to wrap the React frontend and communicate with the Java backend.
*   The ESP32 firmware is written in C++/Arduino.
*   Code is well-documented with comments and separate Markdown files for documentation.

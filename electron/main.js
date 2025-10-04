const { app, BrowserWindow } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const isDev = require('electron-is-dev');

let mainWindow;
let javaProcess;

const API_PORT = 8080;

function findJavaPath() {
  // Try to find Java in common locations
  const { platform } = process;

  if (platform === 'win32') {
    return 'java'; // Assumes Java is in PATH
  } else if (platform === 'darwin') {
    return 'java';
  } else {
    return 'java';
  }
}

function startJavaBackend() {
  return new Promise((resolve, reject) => {
    const javaPath = findJavaPath();
    const jarPath = isDev
      ? path.join(__dirname, '..', 'target', 'robotics-control-system-1.0.0-jar-with-dependencies.jar')
      : path.join(process.resourcesPath, 'backend.jar');

    console.log('[Java Backend] Starting backend...');
    console.log('[Java Backend] JAR path:', jarPath);

    javaProcess = spawn(javaPath, [
      '-cp',
      jarPath,
      'com.soccerbots.control.HeadlessLauncher',
      API_PORT.toString()
    ], {
      stdio: ['ignore', 'pipe', 'pipe']
    });

    javaProcess.stdout.on('data', (data) => {
      console.log(`[Java Backend] ${data.toString().trim()}`);

      // Check if API server started successfully
      if (data.toString().includes('API server running')) {
        console.log('[Java Backend] Backend ready!');
        resolve();
      }
    });

    javaProcess.stderr.on('data', (data) => {
      console.error(`[Java Backend Error] ${data.toString().trim()}`);
    });

    javaProcess.on('error', (error) => {
      console.error('[Java Backend] Failed to start:', error);
      reject(error);
    });

    javaProcess.on('close', (code) => {
      console.log(`[Java Backend] Process exited with code ${code}`);
    });

    // Timeout in case API server message is missed
    setTimeout(() => {
      if (javaProcess && !javaProcess.killed) {
        console.log('[Java Backend] Timeout reached, assuming backend started');
        resolve();
      }
    }, 5000);
  });
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 700,
    backgroundColor: '#0a0a0a',
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, 'preload.js')
    },
    titleBarStyle: 'default',
    frame: true,
    icon: path.join(__dirname, 'build', 'icon.png')
  });

  // Remove menu bar
  mainWindow.setMenuBarVisibility(false);

  const frontendPath = isDev
    ? 'http://localhost:5173' // Vite dev server
    : `file://${path.join(__dirname, '..', 'frontend', 'dist', 'index.html')}`;

  mainWindow.loadURL(frontendPath);

  // Open DevTools in development
  if (isDev) {
    mainWindow.webContents.openDevTools();
  }

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

app.whenReady().then(async () => {
  console.log('[Electron] App ready, starting Java backend...');

  try {
    await startJavaBackend();
    console.log('[Electron] Creating window...');
    createWindow();
  } catch (error) {
    console.error('[Electron] Failed to start backend:', error);
    app.quit();
  }
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

app.on('before-quit', () => {
  console.log('[Electron] Stopping Java backend...');
  if (javaProcess) {
    javaProcess.kill();
  }
});

// Handle Java process crashes
process.on('exit', () => {
  if (javaProcess) {
    javaProcess.kill();
  }
});

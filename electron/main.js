const { app, BrowserWindow } = require('electron');
const path = require('path');
const { spawn } = require('child_process');
const isDev = require('electron-is-dev');

let mainWindow;
let pythonProcess;

const API_PORT = 8080;

function startPythonBackend() {
  // Only start Python backend in production mode
  // In dev mode, it's already started by npm run dev
  if (isDev) {
    console.log('[Python Backend] Development mode - backend should already be running via npm run dev');
    return Promise.resolve();
  }

  return new Promise((resolve, reject) => {
    const pythonPath = process.platform === 'win32' ? 'python' : 'python3';
    const scriptPath = path.join(process.resourcesPath, 'python_backend', 'main.py');

    console.log('[Python Backend] Starting backend...');
    console.log('[Python Backend] Script path:', scriptPath);

    pythonProcess = spawn(pythonPath, [scriptPath, API_PORT.toString()], {
      stdio: ['ignore', 'pipe', 'pipe']
    });

    pythonProcess.stdout.on('data', (data) => {
      console.log(`[Python Backend] ${data.toString().trim()}`);

      // Check if API server started successfully
      if (data.toString().includes('API server running')) {
        console.log('[Python Backend] Backend ready!');
        resolve();
      }
    });

    pythonProcess.stderr.on('data', (data) => {
      console.error(`[Python Backend Error] ${data.toString().trim()}`);
    });

    pythonProcess.on('error', (error) => {
      console.error('[Python Backend] Failed to start:', error);
      reject(error);
    });

    pythonProcess.on('close', (code) => {
      console.log(`[Python Backend] Process exited with code ${code}`);
    });

    // Timeout in case API server message is missed
    setTimeout(() => {
      if (pythonProcess && !pythonProcess.killed) {
        console.log('[Python Backend] Timeout reached, assuming backend started');
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
  console.log('[Electron] App ready...');

  try {
    await startPythonBackend();
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
  console.log('[Electron] Stopping Python backend...');
  if (pythonProcess) {
    pythonProcess.kill();
  }
});

// Handle Python process crashes
process.on('exit', () => {
  if (pythonProcess) {
    pythonProcess.kill();
  }
});

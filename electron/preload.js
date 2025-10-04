const { contextBridge } = require('electron');

// Expose protected methods that allow the renderer process to use
// ipcRenderer without exposing the entire object
contextBridge.exposeInMainWorld('electron', {
  platform: process.platform,
  apiUrl: 'http://localhost:8080'
});

console.log('[Preload] Context bridge initialized');

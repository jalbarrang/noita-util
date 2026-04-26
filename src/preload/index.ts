import { contextBridge, ipcRenderer } from 'electron';
import { electronAPI } from '@electron-toolkit/preload';
import type {
  AppConfig,
  ConfigPathValidationResult,
  MemoryStatus,
  NoitaUtilApi,
} from '../shared/types.js';

// Custom APIs for renderer
const noitaUtil: NoitaUtilApi = {
  config: {
    load: () => ipcRenderer.invoke('config:load') as Promise<AppConfig>,
    save: (config) => ipcRenderer.invoke('config:save', config) as Promise<AppConfig>,
    validatePaths: () =>
      ipcRenderer.invoke('config:validate-paths') as Promise<ConfigPathValidationResult>,
  },
  dialog: {
    selectFolder: () => ipcRenderer.invoke('dialog:select-folder') as Promise<string | undefined>,
    selectFile: (filters) =>
      ipcRenderer.invoke('dialog:select-file', filters) as Promise<string | undefined>,
  },
  activityLog: {
    list: () => ipcRenderer.invoke('activity-log:list'),
  },
  saves: {
    listBackups: () => ipcRenderer.invoke('saves:list-backups'),
    createBackup: (name) => ipcRenderer.invoke('saves:create-backup', name),
    restoreBackup: (name, backupFirst) =>
      ipcRenderer.invoke('saves:restore-backup', name, backupFirst),
    deleteBackups: (names) => ipcRenderer.invoke('saves:delete-backups', names),
  },
  salakieli: {
    decryptAll: () => ipcRenderer.invoke('salakieli:decrypt-all'),
  },
  noitaData: {
    load: () => ipcRenderer.invoke('noita-data:load'),
  },
  noitaProcess: {
    runSeed: (seed) => ipcRenderer.invoke('noita-process:run-seed', seed),
    getMemoryStatus: () => ipcRenderer.invoke('noita-process:get-memory-status'),
    onMemoryStatus: (callback) => {
      const listener = (_event: Electron.IpcRendererEvent, status: MemoryStatus) =>
        callback(status);
      ipcRenderer.on('noita-process:memory-status', listener);
      return () => ipcRenderer.off('noita-process:memory-status', listener);
    },
  },
};

// Use `contextBridge` APIs to expose Electron APIs to
// renderer only if context isolation is enabled, otherwise
// just add to the DOM global.
if (process.contextIsolated) {
  try {
    contextBridge.exposeInMainWorld('electron', electronAPI);
    contextBridge.exposeInMainWorld('noitaUtil', noitaUtil);
  } catch (error) {
    console.error(error);
  }
} else {
  // @ts-ignore (define in dts)
  window.electron = electronAPI;
  // @ts-ignore (define in dts)
  window.noitaUtil = noitaUtil;
}

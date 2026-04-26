import { BrowserWindow, dialog, ipcMain, type OpenDialogOptions } from 'electron';
import { getActivityLog } from '../domains/activity-log/activity-log-service.js';
import { loadConfig, saveConfig, validatePaths } from '../domains/config/config-service.js';
import { loadNoitaData } from '../domains/noita-data/noita-data-service.js';
import { getMemoryStatus, runSeed } from '../domains/noita-process/noita-process-service.js';
import { decryptAllSalakieli } from '../domains/salakieli/salakieli-service.js';
import {
  createBackup,
  deleteBackups,
  listBackups,
  restoreBackup,
} from '../domains/saves/save-service.js';
import type { AppConfig, FilePickerFilter } from '../../shared/types.js';

export function registerIpcHandlers(): void {
  ipcMain.handle('config:load', () => loadConfig());
  ipcMain.handle('config:save', (_event, config: AppConfig) => saveConfig(config));
  ipcMain.handle('config:validate-paths', () => validatePaths());

  ipcMain.handle('dialog:select-folder', async (event) => {
    const browserWindow = BrowserWindow.fromWebContents(event.sender);
    const options: OpenDialogOptions = { properties: ['openDirectory'] };
    const result = browserWindow
      ? await dialog.showOpenDialog(browserWindow, options)
      : await dialog.showOpenDialog(options);
    return result.canceled ? undefined : result.filePaths[0];
  });

  ipcMain.handle('dialog:select-file', async (event, filters: FilePickerFilter[]) => {
    const browserWindow = BrowserWindow.fromWebContents(event.sender);
    const options: OpenDialogOptions = { filters, properties: ['openFile'] };
    const result = browserWindow
      ? await dialog.showOpenDialog(browserWindow, options)
      : await dialog.showOpenDialog(options);
    return result.canceled ? undefined : result.filePaths[0];
  });

  ipcMain.handle('activity-log:list', () => getActivityLog());

  ipcMain.handle('saves:list-backups', () => listBackups());
  ipcMain.handle('saves:create-backup', (_event, name?: string) => createBackup(name));
  ipcMain.handle('saves:restore-backup', (_event, name: string, backupFirst?: boolean) =>
    restoreBackup(name, backupFirst),
  );
  ipcMain.handle('saves:delete-backups', (_event, names: string[]) => deleteBackups(names));

  ipcMain.handle('salakieli:decrypt-all', () => decryptAllSalakieli());
  ipcMain.handle('noita-data:load', () => loadNoitaData());

  ipcMain.handle('noita-process:run-seed', (_event, seed: number) => runSeed(seed));
  ipcMain.handle('noita-process:get-memory-status', () => getMemoryStatus());
}

import type { ElectronAPI } from '@electron-toolkit/preload';
import type { NoitaUtilApi } from '../../shared/types';

declare module '*.css';

declare global {
  interface Window {
    electron: ElectronAPI;
    noitaUtil: NoitaUtilApi;
  }
}

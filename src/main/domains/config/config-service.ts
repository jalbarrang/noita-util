import { access, mkdir, readFile, writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import { homedir } from 'node:os';
import type { AppConfig } from '../../../shared/types.js';
import { APP_NAME } from '../../../shared/types.js';

const home = homedir();
const legacyConfigDir = join(home, '.config', 'com.dimdarkevil', APP_NAME);
const legacyConfigFile = join(legacyConfigDir, `${APP_NAME}-config.json`);

async function exists(path: string): Promise<boolean> {
  try {
    await access(path);
    return true;
  } catch {
    return false;
  }
}

export function getConfigDir(): string {
  return legacyConfigDir;
}

export function getConfigFile(): string {
  return legacyConfigFile;
}

export async function defaultConfig(): Promise<AppConfig> {
  const steamExe = 'C:\\Program Files (x86)\\Steam\\steamapps\\common\\Noita\\noita.exe';

  return {
    winX: -1,
    winY: -1,
    winWidth: -1,
    winHeight: -1,
    noitaSaveFolder: join(home, 'AppData', 'LocalLow', 'Nolla_Games_Noita'),
    noitaBackupFolder: join(home, 'Desktop', 'noita_saves'),
    noitaExeFile: (await exists(steamExe)) ? steamExe : join(home, 'noita.exe'),
    salakieliSplitterPosition: -1,
    qrefSplitterPosition: -1,
    spellTableColumnWidths: [],
    lastSeed: 1948257554,
    memoryUseWarningMb: 1400,
    memoryUseEverySec: 4,
  };
}

export async function loadConfig(): Promise<AppConfig> {
  const fallback = await defaultConfig();

  if (!(await exists(legacyConfigFile))) {
    return fallback;
  }

  const raw = await readFile(legacyConfigFile, 'utf8');
  const parsed = JSON.parse(raw) as Partial<AppConfig>;
  return { ...fallback, ...parsed };
}

export async function saveConfig(config: AppConfig): Promise<AppConfig> {
  await mkdir(legacyConfigDir, { recursive: true });
  await writeFile(legacyConfigFile, `${JSON.stringify(config, null, 2)}\n`, 'utf8');
  return config;
}

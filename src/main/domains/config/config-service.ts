import { access, mkdir, readFile, stat, writeFile } from 'node:fs/promises';
import { basename, join } from 'node:path';
import { homedir } from 'node:os';
import type { AppConfig, ConfigPathValidationResult } from '../../../shared/types.js';
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

export async function validatePaths(): Promise<ConfigPathValidationResult> {
  const config = await loadConfig();
  const errors: ConfigPathValidationResult['errors'] = {};

  const saveFolder = await stat(config.noitaSaveFolder).catch(() => null);
  if (!saveFolder?.isDirectory()) {
    errors.noitaSaveFolder = `Directory does not exist: ${config.noitaSaveFolder}`;
  } else {
    const gunActions = join(config.noitaSaveFolder, 'data', 'scripts', 'gun', 'gun_actions.lua');
    const gunActionsFile = await stat(gunActions).catch(() => null);
    if (!gunActionsFile?.isFile()) {
      errors.noitaSaveFolder = 'Missing game data files in save folder';
    }
  }

  const exeFile = await stat(config.noitaExeFile).catch(() => null);
  if (!exeFile?.isFile()) {
    errors.noitaExeFile = `File does not exist: ${config.noitaExeFile}`;
  } else {
    const exeName = basename(config.noitaExeFile).toLowerCase();
    if (exeName !== 'noita.exe' && exeName !== 'noita_dev.exe') {
      errors.noitaExeFile = `Expected noita.exe or noita_dev.exe, got ${basename(config.noitaExeFile)}`;
    }
  }

  return { valid: Object.keys(errors).length === 0, errors };
}

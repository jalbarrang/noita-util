import { BrowserWindow } from 'electron';
import { spawn } from 'node:child_process';
import { writeFile } from 'node:fs/promises';
import { basename, dirname, join } from 'node:path';
import si from 'systeminformation';
import type { MemoryStatus } from '../../../shared/types.js';
import { loadConfig, saveConfig } from '../config/config-service.js';

let monitor: NodeJS.Timeout | undefined;
let lastWarningAt = 0;

function sendMemoryStatus(status: MemoryStatus): void {
  for (const window of BrowserWindow.getAllWindows()) {
    window.webContents.send('noita-process:memory-status', status);
  }
}

export async function runSeed(seed: number): Promise<void> {
  const config = await loadConfig();
  const exeName = basename(config.noitaExeFile).toLowerCase();
  if (exeName !== 'noita.exe' && exeName !== 'noita_dev.exe') {
    throw new Error('Set correct exe location');
  }

  const seedFile = join(dirname(config.noitaExeFile), 'magic.txt');
  await writeFile(
    seedFile,
    `<MagicNumbers\nWORLD_SEED="${seed}"\n_DEBUG_DONT_LOAD_OTHER_MAGIC_NUMBERS="1"\n_DEBUG_DONT_SAVE_MAGIC_NUMBERS="1" >\n</MagicNumbers>\n`,
    'utf8',
  );

  await saveConfig({ ...config, lastSeed: seed });
  const child = spawn(config.noitaExeFile, ['-no_logo_splashes', '-magic_numbers', seedFile], {
    cwd: dirname(config.noitaExeFile),
    detached: true,
    stdio: 'ignore',
  });
  child.unref();
}

export async function getMemoryStatus(): Promise<MemoryStatus> {
  const config = await loadConfig();
  const processes = await si.processes();
  const noita = processes.list.find((process) => {
    const name = process.name.toLowerCase();
    return name === 'noita' || name === 'noita.exe';
  });

  if (!noita) {
    return {
      message: 'looking for noita process...',
      processFound: false,
      overWarning: false,
    };
  }

  const memoryMb = noita.memRss / 1024;
  const overWarning = memoryMb > config.memoryUseWarningMb;
  const now = Date.now();
  if (overWarning && now > lastWarningAt + config.memoryUseEverySec * 1000) {
    lastWarningAt = now;
  }

  return {
    message: `noita memory: ${memoryMb.toFixed(2)}Mb`,
    processFound: true,
    memoryMb,
    overWarning,
  };
}

export async function startMemoryMonitor(): Promise<void> {
  if (monitor) {
    return;
  }

  monitor = setInterval(() => {
    getMemoryStatus()
      .then(sendMemoryStatus)
      .catch((error: unknown) => {
        sendMemoryStatus({
          message: `exception reading process info: ${error instanceof Error ? error.message : String(error)}`,
          processFound: false,
          overWarning: false,
        });
      });
  }, 1000);
}

export async function stopMemoryMonitor(): Promise<void> {
  if (monitor) {
    clearInterval(monitor);
    monitor = undefined;
  }
}

import AdmZip from "adm-zip";
import { mkdir, readdir, rename, rm, stat } from "node:fs/promises";
import { basename, join, resolve, sep } from "node:path";
import type { BackupEntry } from "../../../shared/types.js";
import { loadConfig } from "../config/config-service.js";
import { logAction } from "../activity-log/activity-log-service.js";

function safeBackupName(name: string): string {
  const trimmed = name.trim();
  if (!trimmed) {
    throw new Error("Backup name is required");
  }
  if (
    trimmed !== basename(trimmed) ||
    trimmed.includes("/") ||
    trimmed.includes("\\")
  ) {
    throw new Error("Backup name cannot include path separators");
  }
  return trimmed.endsWith(".zip") ? trimmed : `${trimmed}.zip`;
}

function defaultBackupName(): string {
  const pad = (n: number) => String(n).padStart(2, "0");
  const d = new Date();
  return `save_${d.getFullYear()}_${pad(d.getMonth() + 1)}_${pad(d.getDate())}_${pad(d.getHours())}_${pad(d.getMinutes())}`;
}

async function toBackupEntry(path: string): Promise<BackupEntry> {
  const info = await stat(path);
  return {
    name: basename(path),
    path,
    size: info.size,
    lastModified: info.mtime.toISOString(),
  };
}

export async function listBackups(): Promise<BackupEntry[]> {
  const config = await loadConfig();
  await mkdir(config.noitaBackupFolder, { recursive: true });

  const entries = await readdir(config.noitaBackupFolder, {
    withFileTypes: true,
  });
  const zipFiles: string[] = [];
  for (const entry of entries) {
    if (entry.isFile() && entry.name.toLowerCase().endsWith(".zip")) {
      zipFiles.push(join(config.noitaBackupFolder, entry.name));
    }
  }

  const backupPromises: Promise<BackupEntry>[] = [];
  for (const zipFile of zipFiles) {
    backupPromises.push(toBackupEntry(zipFile));
  }
  const backups = await Promise.all(backupPromises);

  return backups.sort(
    (a, b) => Date.parse(b.lastModified) - Date.parse(a.lastModified),
  );
}

export async function createBackup(
  name = defaultBackupName(),
): Promise<BackupEntry> {
  const config = await loadConfig();
  const saveFolder = join(config.noitaSaveFolder, "save00");
  const saveInfo = await stat(saveFolder).catch(() => null);
  if (!saveInfo?.isDirectory()) {
    throw new Error(`can't find save folder at ${saveFolder}`);
  }

  await mkdir(config.noitaBackupFolder, { recursive: true });
  const output = join(config.noitaBackupFolder, safeBackupName(name));
  const zip = new AdmZip();
  zip.addLocalFolder(saveFolder, "save00");
  zip.writeZip(output);

  await logAction("BACKUP", basename(output));
  return toBackupEntry(output);
}

export function assertZipEntryInsideDestination(
  destFolder: string,
  entryName: string,
): string {
  const destRoot = resolve(destFolder);
  const destPath = resolve(destFolder, entryName);
  if (destPath !== destRoot && !destPath.startsWith(`${destRoot}${sep}`)) {
    throw new Error(`entry outside target dir: ${entryName}`);
  }
  return destPath;
}

async function extractZipSafely(
  zipFile: string,
  destFolder: string,
): Promise<void> {
  const zip = new AdmZip(zipFile);
  for (const entry of zip.getEntries()) {
    assertZipEntryInsideDestination(destFolder, entry.entryName);
  }
  zip.extractAllTo(destFolder, true);
}

export async function restoreBackup(
  name: string,
  backupFirst = false,
): Promise<void> {
  const config = await loadConfig();
  const zipFile = join(config.noitaBackupFolder, safeBackupName(name));
  const currentSave = join(config.noitaSaveFolder, "save00");
  const backupSave = join(config.noitaSaveFolder, "save00.bak");

  const currentInfo = await stat(currentSave).catch(() => null);
  if (!currentInfo?.isDirectory()) {
    throw new Error(`save file does not exist: ${currentSave}`);
  }

  if (backupFirst) {
    await createBackup();
  }

  await rm(backupSave, { recursive: true, force: true });
  await rename(currentSave, backupSave);

  try {
    await extractZipSafely(zipFile, config.noitaSaveFolder);
    const restoredInfo = await stat(currentSave).catch(() => null);
    if (!restoredInfo?.isDirectory()) {
      throw new Error("save doesn't exist after unzip");
    }
    await rm(backupSave, { recursive: true, force: true });
    await logAction("RESTORE", basename(zipFile));
  } catch (error) {
    await rm(currentSave, { recursive: true, force: true }).catch(
      () => undefined,
    );
    await rename(backupSave, currentSave).catch(() => undefined);
    throw error;
  }
}

export async function deleteBackups(names: string[]): Promise<BackupEntry[]> {
  const config = await loadConfig();
  for (const name of names) {
    const zipName = safeBackupName(name);
    await rm(join(config.noitaBackupFolder, zipName), { force: false });
    await logAction("DELETE", zipName);
  }
  return listBackups();
}

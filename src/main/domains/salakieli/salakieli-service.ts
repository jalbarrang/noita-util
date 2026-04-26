import { createDecipheriv } from 'node:crypto';
import { readFile } from 'node:fs/promises';
import { join } from 'node:path';
import { loadConfig } from '../config/config-service.js';

type SalakieliFile = {
  fileName: string;
  keyStr: string;
  ivStr: string;
};

const salakieliFiles: Record<string, SalakieliFile> = {
  stats: {
    fileName: 'stats/_stats.salakieli',
    keyStr: '536563726574734f66546865416c6c53',
    ivStr: '54687265654579657341726557617463',
  },
  streaks: {
    fileName: 'stats/_streaks.salakieli',
    keyStr: '536563726574734f66546865416c6c53',
    ivStr: '54687265654579657341726557617463',
  },
  magic_numbers: {
    fileName: 'magic_numbers.salakieli',
    keyStr: '4b6e6f776c6564676549735468654869',
    ivStr: '57686f576f756c646e74476976654576',
  },
  session_numbers: {
    fileName: 'session_numbers.salakieli',
    keyStr: '4b6e6f776c6564676549735468654869',
    ivStr: '57686f576f756c646e74476976654576',
  },
  internal_alchemy_list: {
    fileName: '?',
    keyStr: '31343439363631363932313933343032',
    ivStr: '38313632343338393133393638333733',
  },
};

export async function decryptSalakieliFile(name: string): Promise<string> {
  const config = await loadConfig();
  const fileInfo = salakieliFiles[name];
  if (!fileInfo) {
    throw new Error(`Unknown Salakieli file: ${name}`);
  }

  const content = await readFile(join(config.noitaSaveFolder, 'save00', fileInfo.fileName));
  const decipher = createDecipheriv(
    'aes-128-ctr',
    Buffer.from(fileInfo.keyStr, 'hex'),
    Buffer.from(fileInfo.ivStr, 'hex'),
  );

  return Buffer.concat([decipher.update(content), decipher.final()]).toString('utf8');
}

export async function decryptAllSalakieli(): Promise<Record<string, string>> {
  const results: Record<string, string> = {};

  for (const name of Object.keys(salakieliFiles)) {
    try {
      results[name] = await decryptSalakieliFile(name);
    } catch {
      // Match the legacy behavior: missing or malformed files are omitted.
    }
  }

  return results;
}

import { mkdir, mkdtemp, readFile, rm, writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import { tmpdir } from 'node:os';
import { afterEach, describe, expect, it } from 'vitest';
import {
  assertZipEntryInsideDestination,
  extractZipSafely,
  zipFolder,
} from './save-service.js';

describe('save service', () => {
  const tempDirs: string[] = [];

  async function makeTempDir(): Promise<string> {
    const dir = await mkdtemp(join(tmpdir(), 'noita-util-save-'));
    tempDirs.push(dir);
    return dir;
  }

  afterEach(async () => {
    for (const dir of tempDirs) {
      await rm(dir, { recursive: true, force: true }).catch(() => undefined);
    }
    tempDirs.length = 0;
  });

  it('rejects zip entries outside the destination directory', async () => {
    const dest = await makeTempDir();

    expect(assertZipEntryInsideDestination(dest, 'save00/player.xml')).toContain(dest);
    expect(() => assertZipEntryInsideDestination(dest, '../evil.txt')).toThrow(
      /outside target dir/,
    );
  });

  it('creates a zip from a folder', async () => {
    const tempDir = await makeTempDir();
    const sourceDir = join(tempDir, 'source');
    await mkdir(join(sourceDir, 'nested'), { recursive: true });
    await writeFile(join(sourceDir, 'file.txt'), 'hello');
    await writeFile(join(sourceDir, 'nested', 'deep.txt'), 'world');

    const zipPath = join(tempDir, 'output.zip');
    await zipFolder(sourceDir, 'save00', zipPath);

    const { stat } = await import('node:fs/promises');
    const info = await stat(zipPath);
    expect(info.size).toBeGreaterThan(0);
  });

  it('extracts a zip preserving folder structure', async () => {
    const tempDir = await makeTempDir();
    const sourceDir = join(tempDir, 'source');
    await mkdir(join(sourceDir, 'nested'), { recursive: true });
    await writeFile(join(sourceDir, 'file.txt'), 'hello');
    await writeFile(join(sourceDir, 'nested', 'deep.txt'), 'world');

    const zipPath = join(tempDir, 'output.zip');
    await zipFolder(sourceDir, 'save00', zipPath);

    const extractDir = join(tempDir, 'extracted');
    await mkdir(extractDir, { recursive: true });
    await extractZipSafely(zipPath, extractDir);

    const fileContent = await readFile(join(extractDir, 'save00', 'file.txt'), 'utf-8');
    expect(fileContent).toBe('hello');

    const deepContent = await readFile(join(extractDir, 'save00', 'nested', 'deep.txt'), 'utf-8');
    expect(deepContent).toBe('world');
  });

  it('round-trips zip and extract with identical content', async () => {
    const tempDir = await makeTempDir();
    const sourceDir = join(tempDir, 'source');
    await mkdir(join(sourceDir, 'subdir'), { recursive: true });

    const files = {
      'player.xml': '<player health="100"/>',
      'subdir/world.dat': 'binary-like-data-here\x00\x01\x02',
    };

    for (const [name, content] of Object.entries(files)) {
      await writeFile(join(sourceDir, name), content);
    }

    const zipPath = join(tempDir, 'roundtrip.zip');
    await zipFolder(sourceDir, 'save00', zipPath);

    const extractDir = join(tempDir, 'extracted');
    await mkdir(extractDir, { recursive: true });
    await extractZipSafely(zipPath, extractDir);

    for (const [name, expectedContent] of Object.entries(files)) {
      const actual = await readFile(join(extractDir, 'save00', name), 'utf-8');
      expect(actual).toBe(expectedContent);
    }
  });
});

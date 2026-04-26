import { mkdtemp } from 'node:fs/promises';
import { join } from 'node:path';
import { tmpdir } from 'node:os';
import { describe, expect, it } from 'vitest';
import { assertZipEntryInsideDestination } from './save-service.js';

describe('save service', () => {
  it('rejects zip entries outside the destination directory', async () => {
    const dest = await mkdtemp(join(tmpdir(), 'noita-util-save-'));

    expect(assertZipEntryInsideDestination(dest, 'save00/player.xml')).toContain(dest);
    expect(() => assertZipEntryInsideDestination(dest, '../evil.txt')).toThrow(
      /outside target dir/,
    );
  });
});

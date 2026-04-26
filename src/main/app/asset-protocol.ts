import { protocol } from 'electron';
import { readFile } from 'node:fs/promises';
import { dirname, extname, normalize, resolve, sep } from 'node:path';
import { loadConfig } from '../domains/config/config-service.js';

const noitaAssetScheme = 'noita-asset';
const allowedAssetPrefixes = ['data/', 'mods/'];
const contentTypes = new Map([
  ['.png', 'image/png'],
  ['.jpg', 'image/jpeg'],
  ['.jpeg', 'image/jpeg'],
  ['.webp', 'image/webp'],
  ['.gif', 'image/gif'],
]);

export function registerNoitaAssetScheme(): void {
  protocol.registerSchemesAsPrivileged([
    {
      scheme: noitaAssetScheme,
      privileges: {
        secure: true,
        standard: true,
        supportFetchAPI: true,
      },
    },
  ]);
}

export function registerNoitaAssetProtocol(): void {
  protocol.handle(noitaAssetScheme, async (request) => {
    try {
      const assetPath = decodeAssetPath(request.url);
      const config = await loadConfig();
      const installDir = dirname(config.noitaExeFile);
      const filePath = resolve(installDir, assetPath);

      if (!isSafeAssetPath(assetPath, installDir, filePath)) {
        return new Response('Invalid asset path', { status: 400 });
      }

      const extension = extname(filePath).toLowerCase();
      const contentType = contentTypes.get(extension);
      if (!contentType) {
        return new Response('Unsupported asset type', { status: 415 });
      }

      const asset = await readFile(filePath);
      return new Response(asset, {
        status: 200,
        headers: {
          'content-type': contentType,
          'cache-control': 'public, max-age=86400',
        },
      });
    } catch (error) {
      return new Response(error instanceof Error ? error.message : 'Asset not found', { status: 404 });
    }
  });
}

function decodeAssetPath(url: string): string {
  const pathname = decodeURIComponent(new URL(url).pathname).replace(/^\/+/u, '');
  return pathname.replace(/\\/gu, '/');
}

function isSafeAssetPath(assetPath: string, installDir: string, filePath: string): boolean {
  const normalizedAssetPath = normalize(assetPath).replace(/\\/gu, '/');

  if (!normalizedAssetPath || normalizedAssetPath.startsWith('/') || normalizedAssetPath.includes('\0')) {
    return false;
  }

  if (!allowedAssetPrefixes.some((prefix) => normalizedAssetPath.startsWith(prefix))) {
    return false;
  }

  return filePath === installDir || filePath.startsWith(`${installDir}${sep}`);
}

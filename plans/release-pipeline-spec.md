# Release Pipeline Spec: Electron Builder + release-please + GitHub Actions

## Overview

Set up an automated release pipeline for `noita-util` (Electron app, Windows-only) using:

- **release-please** for version management, changelog generation, and tag creation
- **electron-builder** for producing Windows binaries (NSIS installer + portable)
- **GitHub Actions** for CI orchestration

The pipeline has two workflows:
1. **release-please** - runs on every push to `main`, maintains a "Release PR" that accumulates conventional commits, and when merged creates a git tag + GitHub Release.
2. **build** - triggered by the tag that release-please creates, builds the Electron app and uploads artifacts to the GitHub Release.

---

## Prerequisites

- Repository uses **conventional commits** (`feat:`, `fix:`, `chore:`, etc.) so release-please can determine version bumps.
- The current version in `package.json` is `2.0.0`. Existing tags go up to `v1.2.2`. release-please will pick up from the version in its manifest.

---

## 1. release-please Configuration

### 1.1 Create `release-please-config.json` (repo root)

```json
{
  "$schema": "https://raw.githubusercontent.com/googleapis/release-please/main/schemas/config.json",
  "release-type": "node",
  "packages": {
    ".": {
      "component": "noita-util",
      "changelog-path": "CHANGELOG.md",
      "bump-minor-pre-major": true,
      "bump-patch-for-minor-pre-major": false
    }
  },
  "include-component-in-tag": false,
  "tag-separator": "",
  "pull-request-title-pattern": "chore: release ${version}",
  "pull-request-header": ":rocket: Release is ready",
  "changelog-sections": [
    { "type": "feat", "section": "Features" },
    { "type": "fix", "section": "Bug Fixes" },
    { "type": "perf", "section": "Performance" },
    { "type": "refactor", "section": "Refactoring" },
    { "type": "docs", "section": "Documentation" },
    { "type": "chore", "section": "Miscellaneous", "hidden": true }
  ]
}
```

Key decisions:
- `"release-type": "node"` - release-please knows how to bump `version` in `package.json`.
- `"include-component-in-tag": false` - tags will be `v2.1.0`, not `noita-util-v2.1.0`.
- `"bump-minor-pre-major": true` - while in early development, breaking changes bump minor instead of major (remove this once stable if desired).

### 1.2 Create `.release-please-manifest.json` (repo root)

```json
{
  ".": "2.0.0"
}
```

This tells release-please the current version so it doesn't try to infer from tags. It will be automatically updated by release-please on each release.

---

## 2. GitHub Actions Workflows

### 2.1 Release Please Workflow

**File:** `.github/workflows/release-please.yml`

```yaml
name: Release Please

on:
  push:
    branches:
      - main

permissions:
  contents: write
  pull-requests: write

jobs:
  release-please:
    runs-on: ubuntu-latest
    outputs:
      release_created: ${{ steps.release.outputs.release_created }}
      tag_name: ${{ steps.release.outputs.tag_name }}
      version: ${{ steps.release.outputs.version }}
    steps:
      - uses: googleapis/release-please-action@v4
        id: release
        with:
          token: ${{ secrets.GITHUB_TOKEN }}
```

Notes:
- release-please-action v4 reads config from `release-please-config.json` and manifest from `.release-please-manifest.json` automatically.
- When the release PR is merged, this job will output `release_created: true` and the `tag_name` (e.g., `v2.1.0`).

### 2.2 Build & Publish Workflow

**File:** `.github/workflows/build.yml`

This workflow is triggered when a tag matching `v*` is pushed (which release-please does automatically on PR merge).

```yaml
name: Build & Publish

on:
  push:
    tags:
      - 'v*'

permissions:
  contents: write

jobs:
  build-windows:
    runs-on: windows-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Bun
        uses: oven-sh/setup-bun@v2
        with:
          bun-version: '1.3.13'

      - name: Install dependencies
        run: bun install --frozen-lockfile

      - name: Typecheck
        run: bun run typecheck

      - name: Run tests
        run: bun run test

      - name: Build Electron app
        run: bun run build

      - name: Package with electron-builder
        run: bun run electron-builder --win nsis portable --publish never

      - name: Upload artifacts to GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: |
            dist/*.exe
            dist/*.yml
            dist/*.yaml
          tag_name: ${{ github.ref_name }}
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

Key decisions:
- **`runs-on: windows-latest`** - Electron Windows builds should run on Windows to avoid cross-compilation issues.
- **`bun-version: '1.3.13'`** - matches `packageManager` in `package.json`.
- **`bun run electron-builder --win nsis portable --publish never`** - builds both the NSIS installer and portable exe using the local `electron-builder` devDependency. `--publish never` keeps packaging and release upload as separate steps so electron-builder does not auto-publish duplicate assets on tagged CI builds.
- **`softprops/action-gh-release`** - uploads the built artifacts to the GitHub Release that release-please already created. The `dist/` directory is electron-builder's default output. The `*.yml`/`*.yaml` files are the `latest.yml` auto-update manifests.
- **No code signing** is configured at this time.

---

## 3. Update `electron-builder.yml`

The current config has an `appImage` section (Linux) which is unnecessary since this is Windows-only. Clean it up and add the `portable` target config:

```yaml
appId: com.jalbarrang.noita-util
productName: noita-util
directories:
  buildResources: build

files:
  - '!**/.vscode/*'
  - '!src/*'
  - '!electron.vite.config.{js,ts,mjs,cjs}'
  - '!{.eslintcache,eslint.config.mjs,.prettierignore,.prettierrc.yaml,dev-app-update.yml,CHANGELOG.md,README.md}'
  - '!{.env,.env.*,.npmrc,pnpm-lock.yaml}'
  - '!{tsconfig.json,tsconfig.node.json,tsconfig.web.json}'
  - '!legacy/*'

asarUnpack:
  - resources/**

win:
  executableName: noita-util
  target:
    - nsis
    - portable

nsis:
  artifactName: ${name}-${version}-setup.${ext}
  shortcutName: ${productName}
  uninstallDisplayName: ${productName}
  createDesktopShortcut: always

portable:
  artifactName: ${name}-${version}-portable.${ext}

npmRebuild: false

publish:
  provider: github
  owner: jalbarrang
  repo: noita-util
```

Changes from current:
- **Removed** the `appImage` section (Linux-only, not needed).
- **Added** `target: [nsis, portable]` under `win` to explicitly declare both targets.
- **Added** `portable` section with an artifact name pattern.
- **Updated** `publish` to use explicit `owner`/`repo` instead of a full URL (more standard for electron-builder).

---

## 4. Files to Create / Modify (Summary)

| File | Action | Description |
|------|--------|-------------|
| `release-please-config.json` | **Create** | release-please configuration |
| `.release-please-manifest.json` | **Create** | Version manifest (starts at `2.0.0`) |
| `.github/workflows/release-please.yml` | **Create** | Workflow that manages release PRs |
| `.github/workflows/build.yml` | **Create** | Workflow that builds on tag push |
| `electron-builder.yml` | **Modify** | Remove appImage, add portable target, clean up publish config |

---

## 5. Flow Diagram

```
Developer pushes to main (conventional commits)
        |
        v
[release-please.yml] runs
        |
        v
release-please opens/updates a "Release PR"
  - bumps version in package.json
  - updates CHANGELOG.md
  - PR title: "chore: release 2.1.0"
        |
        v
Developer reviews & merges the Release PR
        |
        v
[release-please.yml] runs again on the merge commit
  - creates tag v2.1.0
  - creates GitHub Release (draft=false, with changelog body)
        |
        v
Tag push triggers [build.yml]
        |
        v
build-windows job:
  1. checkout code
  2. setup bun 1.3.13
  3. bun install --frozen-lockfile
  4. typecheck
  5. run tests
  6. electron-vite build
  7. electron-builder --win nsis portable
  8. upload .exe + update manifests to GitHub Release
        |
        v
GitHub Release now has:
  - noita-util-2.1.0-setup.exe (NSIS installer)
  - noita-util-2.1.0-portable.exe (portable)
  - latest.yml (auto-update manifest)
  - Source code archives (auto-attached by GitHub)
```

---

## 6. Optional Future Enhancements (Not in scope now)

- **Code signing**: Add Windows code signing via `CSC_LINK` / `CSC_KEY_PASSWORD` secrets and `win.certificateFile` in electron-builder config.
- **Auto-update**: The `publish` config + `latest.yml` artifact enables electron-updater. The app would need `autoUpdater` code in the main process.
- **macOS / Linux builds**: Add matrix strategy with `macos-latest` / `ubuntu-latest` runners if cross-platform support is needed later.
- **CI on PRs**: A separate workflow that runs typecheck + tests on every PR (not just releases).

---

## 7. Implementation Notes for the Coding Agent

1. **Create files in order**: release-please config files first, then workflows, then modify electron-builder.yml.
2. **Do not run `bun run build`** to verify - per project conventions, typechecking (`bun run typecheck`) is sufficient for validation.
3. **Use `bun`** for all package management operations, never npm/yarn/pnpm.
4. **The `.github/workflows/` directory does not exist yet** - it needs to be created.
5. **Do not modify `package.json` version** - release-please will handle version bumps going forward.
6. **Conventional commits**: The team should start using conventional commit messages (`feat:`, `fix:`, `chore:`, etc.) for release-please to work correctly. This is a process change, not a code change.

import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { createFileRoute } from '@tanstack/react-router';
import { FolderOpenIcon, RefreshCwIcon, RotateCcwIcon, SaveIcon } from 'lucide-react';
import { toast } from 'sonner';

import { Button } from '@renderer/components/ui/button';
import { Input } from '@renderer/components/ui/input';
import { Label } from '@renderer/components/ui/label';
import { Separator } from '@renderer/components/ui/separator';
import type { AppConfig } from '../../../shared/types';

export const Route = createFileRoute('/settings')({
  component: SettingsPage,
});

const configQueryKey = ['config'];
const configValidationQueryKey = ['config', 'validation'];
const noitaExeFilters = [{ name: 'Noita executable', extensions: ['exe'] }];

type SettingsForm = {
  noitaSaveFolder: string;
  noitaBackupFolder: string;
  noitaExeFile: string;
  memoryUseWarningMb: string;
  memoryUseEverySec: string;
};

function SettingsPage() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState<SettingsForm | null>(null);
  const [busy, setBusy] = useState(false);

  const configQuery = useQuery({
    queryKey: configQueryKey,
    queryFn: () => window.noitaUtil.config.load(),
  });

  useEffect(() => {
    if (configQuery.data) {
      setForm(toSettingsForm(configQuery.data));
    }
  }, [configQuery.data]);

  const hasChanges = Boolean(form && configQuery.data && !formsMatchConfig(form, configQuery.data));

  const updateForm = <Key extends keyof SettingsForm>(key: Key, value: SettingsForm[Key]) => {
    setForm((current) => (current ? { ...current, [key]: value } : current));
  };

  const browseFolder = async (key: 'noitaSaveFolder' | 'noitaBackupFolder') => {
    try {
      const selected = await window.noitaUtil.dialog.selectFolder();
      if (selected) updateForm(key, selected);
    } catch (error) {
      toast.error('Could not select folder', { description: getErrorMessage(error) });
    }
  };

  const browseExe = async () => {
    try {
      const selected = await window.noitaUtil.dialog.selectFile(noitaExeFilters);
      if (selected) updateForm('noitaExeFile', selected);
    } catch (error) {
      toast.error('Could not select executable', { description: getErrorMessage(error) });
    }
  };

  const resetForm = () => {
    if (configQuery.data) setForm(toSettingsForm(configQuery.data));
  };

  const saveSettings = async () => {
    if (!form || !configQuery.data) return;

    const memoryUseWarningMb = parsePositiveInteger(
      form.memoryUseWarningMb,
      'Memory warning threshold',
    );
    if (memoryUseWarningMb === undefined) return;

    const memoryUseEverySec = parsePositiveInteger(form.memoryUseEverySec, 'Memory check interval');
    if (memoryUseEverySec === undefined) return;

    setBusy(true);
    try {
      const saved = await window.noitaUtil.config.save({
        ...configQuery.data,
        noitaSaveFolder: form.noitaSaveFolder.trim(),
        noitaBackupFolder: form.noitaBackupFolder.trim(),
        noitaExeFile: form.noitaExeFile.trim(),
        memoryUseWarningMb,
        memoryUseEverySec,
      });
      queryClient.setQueryData(configQueryKey, saved);
      void queryClient.invalidateQueries({ queryKey: configValidationQueryKey });
      setForm(toSettingsForm(saved));
      toast.success('Settings saved');
    } catch (error) {
      toast.error('Could not save settings', { description: getErrorMessage(error) });
    } finally {
      setBusy(false);
    }
  };

  if (configQuery.isError) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-3 text-sm text-muted-foreground">
        <div>Could not load settings.</div>
        <Button variant="outline" onClick={() => void configQuery.refetch()}>
          <RefreshCwIcon data-icon="inline-start" />
          retry
        </Button>
      </div>
    );
  }

  if (configQuery.isLoading || !form) {
    return (
      <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
        Loading settings...
      </div>
    );
  }

  return (
    <div className="flex h-full flex-col">
      <div className="flex h-12 items-center justify-between border-b px-4">
        <div>
          <h1 className="text-sm font-medium">settings</h1>
          <p className="text-xs text-muted-foreground">configure Noita paths and memory warnings</p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={() => void configQuery.refetch()}
            disabled={busy || configQuery.isFetching}
          >
            <RefreshCwIcon data-icon="inline-start" />
            refresh
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={resetForm}
            disabled={busy || !hasChanges}
          >
            <RotateCcwIcon data-icon="inline-start" />
            reset
          </Button>
          <Button type="button" onClick={() => void saveSettings()} disabled={busy || !hasChanges}>
            <SaveIcon data-icon="inline-start" />
            save
          </Button>
        </div>
      </div>

      <div className="min-h-0 flex-1 overflow-auto p-4">
        <form
          className="mx-auto flex max-w-3xl flex-col gap-6"
          onSubmit={(event) => {
            event.preventDefault();
            void saveSettings();
          }}
        >
          <section className="flex flex-col gap-4">
            <div className="flex flex-col gap-1">
              <h2 className="text-sm font-medium">paths</h2>
              <p className="text-xs text-muted-foreground">
                These locations are used for backups, game data, seed launching, and save-file
                tools.
              </p>
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="noita-save-folder">Noita save folder</Label>
              <div className="flex gap-2">
                <Input
                  id="noita-save-folder"
                  value={form.noitaSaveFolder}
                  onChange={(event) => updateForm('noitaSaveFolder', event.target.value)}
                  className="font-mono"
                  placeholder="Path to Nolla_Games_Noita"
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => void browseFolder('noitaSaveFolder')}
                  disabled={busy}
                >
                  <FolderOpenIcon data-icon="inline-start" />
                  browse
                </Button>
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="noita-backup-folder">Backup folder</Label>
              <div className="flex gap-2">
                <Input
                  id="noita-backup-folder"
                  value={form.noitaBackupFolder}
                  onChange={(event) => updateForm('noitaBackupFolder', event.target.value)}
                  className="font-mono"
                  placeholder="Path to backup destination"
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => void browseFolder('noitaBackupFolder')}
                  disabled={busy}
                >
                  <FolderOpenIcon data-icon="inline-start" />
                  browse
                </Button>
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="noita-exe-file">Noita executable</Label>
              <div className="flex gap-2">
                <Input
                  id="noita-exe-file"
                  value={form.noitaExeFile}
                  onChange={(event) => updateForm('noitaExeFile', event.target.value)}
                  className="font-mono"
                  placeholder="Path to noita.exe"
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => void browseExe()}
                  disabled={busy}
                >
                  <FolderOpenIcon data-icon="inline-start" />
                  browse
                </Button>
              </div>
            </div>
          </section>

          <Separator />

          <section className="flex flex-col gap-4">
            <div className="flex flex-col gap-1">
              <h2 className="text-sm font-medium">memory monitor</h2>
              <p className="text-xs text-muted-foreground">
                The footer warns when the running Noita process exceeds the configured threshold.
              </p>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="flex flex-col gap-2">
                <Label htmlFor="memory-use-warning-mb">Warning threshold (MB)</Label>
                <Input
                  id="memory-use-warning-mb"
                  type="number"
                  min={1}
                  step={1}
                  value={form.memoryUseWarningMb}
                  onChange={(event) => updateForm('memoryUseWarningMb', event.target.value)}
                />
              </div>

              <div className="flex flex-col gap-2">
                <Label htmlFor="memory-use-every-sec">Warning interval (seconds)</Label>
                <Input
                  id="memory-use-every-sec"
                  type="number"
                  min={1}
                  step={1}
                  value={form.memoryUseEverySec}
                  onChange={(event) => updateForm('memoryUseEverySec', event.target.value)}
                />
              </div>
            </div>
          </section>
        </form>
      </div>
    </div>
  );
}

function toSettingsForm(config: AppConfig): SettingsForm {
  return {
    noitaSaveFolder: config.noitaSaveFolder,
    noitaBackupFolder: config.noitaBackupFolder,
    noitaExeFile: config.noitaExeFile,
    memoryUseWarningMb: String(config.memoryUseWarningMb),
    memoryUseEverySec: String(config.memoryUseEverySec),
  };
}

function formsMatchConfig(form: SettingsForm, config: AppConfig) {
  return (
    form.noitaSaveFolder === config.noitaSaveFolder &&
    form.noitaBackupFolder === config.noitaBackupFolder &&
    form.noitaExeFile === config.noitaExeFile &&
    form.memoryUseWarningMb === String(config.memoryUseWarningMb) &&
    form.memoryUseEverySec === String(config.memoryUseEverySec)
  );
}

function parsePositiveInteger(value: string, label: string) {
  const parsed = Number(value);
  if (!Number.isInteger(parsed) || parsed < 1) {
    toast.error('Invalid setting', { description: `${label} must be a positive whole number.` });
    return undefined;
  }
  return parsed;
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Unknown error';
}

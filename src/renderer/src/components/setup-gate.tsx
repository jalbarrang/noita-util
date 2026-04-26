import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { AlertTriangleIcon, FolderOpenIcon, SaveIcon } from 'lucide-react';
import { toast } from 'sonner';

import { Button } from '@renderer/components/ui/button';
import { Input } from '@renderer/components/ui/input';
import { Label } from '@renderer/components/ui/label';
import type { AppConfig, ConfigPathValidationResult } from '../../../shared/types';

const configQueryKey = ['config'];
const configValidationQueryKey = ['config', 'validation'];
const noitaExeFilters = [{ name: 'Noita executable', extensions: ['exe'] }];

type SetupGateProps = {
  config: AppConfig;
  validation: ConfigPathValidationResult;
};

type SetupForm = {
  noitaSaveFolder: string;
  noitaExeFile: string;
};

export function SetupGate(props: SetupGateProps) {
  const { config, validation } = props;
  const queryClient = useQueryClient();
  const [form, setForm] = useState<SetupForm>(() => toSetupForm(config));
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    setForm(toSetupForm(config));
  }, [config]);

  const updateForm = <Key extends keyof SetupForm>(key: Key, value: SetupForm[Key]) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const browseSaveFolder = async () => {
    try {
      const selected = await window.noitaUtil.dialog.selectFolder();
      if (selected) updateForm('noitaSaveFolder', selected);
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

  const saveAndContinue = async () => {
    setBusy(true);
    try {
      const saved = await window.noitaUtil.config.save({
        ...config,
        noitaSaveFolder: form.noitaSaveFolder.trim(),
        noitaExeFile: form.noitaExeFile.trim(),
      });
      queryClient.setQueryData(configQueryKey, saved);
      await queryClient.invalidateQueries({ queryKey: configValidationQueryKey });
    } catch (error) {
      toast.error('Could not save setup', { description: getErrorMessage(error) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="flex h-full items-center justify-center p-4">
      <form
        className="w-full max-w-2xl rounded-xl border bg-card shadow-sm"
        onSubmit={(event) => {
          event.preventDefault();
          void saveAndContinue();
        }}
      >
        <div className="border-b p-5">
          <div className="flex items-start gap-3">
            <div className="mt-0.5 rounded-full border border-destructive/30 bg-destructive/10 p-2 text-destructive">
              <AlertTriangleIcon className="size-4" />
            </div>
            <div className="flex flex-col gap-1">
              <h1 className="text-base font-semibold tracking-tight">setup required</h1>
              <p className="max-w-xl text-sm text-muted-foreground">
                Noita game paths need to be configured before noita-util can load game data.
              </p>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-5 p-5">
          <PathField
            id="setup-noita-save-folder"
            label="Noita save folder"
            value={form.noitaSaveFolder}
            placeholder="Path to Nolla_Games_Noita"
            error={validation.errors.noitaSaveFolder}
            disabled={busy}
            onChange={(value) => updateForm('noitaSaveFolder', value)}
            onBrowse={() => void browseSaveFolder()}
          />

          <PathField
            id="setup-noita-exe-file"
            label="Noita executable"
            value={form.noitaExeFile}
            placeholder="Path to noita.exe"
            error={validation.errors.noitaExeFile}
            disabled={busy}
            onChange={(value) => updateForm('noitaExeFile', value)}
            onBrowse={() => void browseExe()}
          />
        </div>

        <div className="flex items-center justify-end gap-2 border-t p-5">
          <Button type="submit" disabled={busy}>
            <SaveIcon data-icon="inline-start" />
            {busy ? 'saving...' : 'save and continue'}
          </Button>
        </div>
      </form>
    </div>
  );
}

type PathFieldProps = {
  id: string;
  label: string;
  value: string;
  placeholder: string;
  error: string | undefined;
  disabled: boolean;
  onChange: (value: string) => void;
  onBrowse: () => void;
};

function PathField(props: PathFieldProps) {
  const { id, label, value, placeholder, error, disabled, onChange, onBrowse } = props;

  return (
    <div className="flex flex-col gap-2 rounded-lg border bg-background/50 p-3">
      <div className="flex items-center justify-between gap-2">
        <Label htmlFor={id}>{label}</Label>
        {error ? (
          <span className="inline-flex items-center gap-1 text-xs text-destructive">
            <AlertTriangleIcon className="size-3" />
            needs attention
          </span>
        ) : null}
      </div>
      <div className="flex gap-2">
        <Input
          id={id}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          className="font-mono"
          placeholder={placeholder}
          aria-invalid={Boolean(error)}
          disabled={disabled}
        />
        <Button type="button" variant="outline" onClick={onBrowse} disabled={disabled}>
          <FolderOpenIcon data-icon="inline-start" />
          browse
        </Button>
      </div>
      {error ? <p className="text-xs text-destructive">{error}</p> : null}
    </div>
  );
}

function toSetupForm(config: AppConfig): SetupForm {
  return {
    noitaSaveFolder: config.noitaSaveFolder,
    noitaExeFile: config.noitaExeFile,
  };
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Unknown error';
}

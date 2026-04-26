import { createFileRoute } from '@tanstack/react-router';
import { ArchiveIcon, FileClockIcon, RefreshCwIcon, RotateCcwIcon, Trash2Icon } from 'lucide-react';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { toast } from 'sonner';

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@renderer/components/ui/alert-dialog';
import { Button } from '@renderer/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@renderer/components/ui/dialog';
import { Input } from '@renderer/components/ui/input';
import { Label } from '@renderer/components/ui/label';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@renderer/components/ui/table';
import type { Activity, BackupEntry } from '../../../shared/types';

export const Route = createFileRoute('/')({
  component: HomePage,
});

function HomePage() {
  const [backups, setBackups] = useState<BackupEntry[]>([]);
  const [selectedNames, setSelectedNames] = useState<Set<string>>(() => new Set());
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [restoreOpen, setRestoreOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [activityOpen, setActivityOpen] = useState(false);
  const [activityLog, setActivityLog] = useState<Activity[]>([]);
  const [backupName, setBackupName] = useState(defaultBackupName);

  const selectedBackups = useMemo(
    () => backups.filter((backup) => selectedNames.has(backup.name)),
    [backups, selectedNames],
  );

  const sortedBackups = useMemo(
    () => [...backups].sort((left, right) => Date.parse(right.lastModified) - Date.parse(left.lastModified)),
    [backups],
  );

  const loadBackups = useCallback(async () => {
    setLoading(true);
    try {
      const entries = await window.noitaUtil.saves.listBackups();
      setBackups(entries);
      setSelectedNames((current) => new Set([...current].filter((name) => entries.some((entry) => entry.name === name))));
    } catch (error) {
      toast.error('Could not load backups', { description: getErrorMessage(error) });
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadBackups();
  }, [loadBackups]);

  const toggleBackup = (name: string) => {
    setSelectedNames((current) => {
      const next = new Set(current);
      if (next.has(name)) next.delete(name);
      else next.add(name);
      return next;
    });
  };

  const toggleAllBackups = () => {
    setSelectedNames((current) => {
      if (current.size === sortedBackups.length) return new Set();
      return new Set(sortedBackups.map((backup) => backup.name));
    });
  };

  const openCreateDialog = () => {
    setBackupName(defaultBackupName());
    setCreateOpen(true);
  };

  const createBackup = async () => {
    setBusy(true);
    try {
      const backup = await window.noitaUtil.saves.createBackup(backupName.trim() || undefined);
      toast.success('Backup created', { description: backup.name });
      setCreateOpen(false);
      await loadBackups();
    } catch (error) {
      toast.error('Could not create backup', { description: getErrorMessage(error) });
    } finally {
      setBusy(false);
    }
  };

  const restoreBackup = async (backupFirst: boolean) => {
    const [backup] = selectedBackups;
    if (!backup) return;

    setBusy(true);
    try {
      await window.noitaUtil.saves.restoreBackup(backup.name, backupFirst);
      toast.success('Backup restored', { description: backup.name });
      setRestoreOpen(false);
      await loadBackups();
    } catch (error) {
      toast.error('Could not restore backup', { description: getErrorMessage(error) });
    } finally {
      setBusy(false);
    }
  };

  const deleteBackups = async () => {
    const names = selectedBackups.map((backup) => backup.name);
    if (names.length === 0) return;

    setBusy(true);
    try {
      await window.noitaUtil.saves.deleteBackups(names);
      toast.success('Backups deleted', { description: `${names.length} removed` });
      setDeleteOpen(false);
      setSelectedNames(new Set());
      await loadBackups();
    } catch (error) {
      toast.error('Could not delete backups', { description: getErrorMessage(error) });
    } finally {
      setBusy(false);
    }
  };

  const openActivityLog = async () => {
    setActivityOpen(true);
    try {
      const log = await window.noitaUtil.activityLog.list();
      setActivityLog([...log.activities].sort((left, right) => Date.parse(right.dateTime) - Date.parse(left.dateTime)));
    } catch (error) {
      toast.error('Could not load activity log', { description: getErrorMessage(error) });
    }
  };

  return (
    <div className="flex h-full flex-col">
      <div className="flex h-12 items-center justify-between border-b px-4">
        <div className="flex items-center gap-2">
          <Button onClick={openCreateDialog} disabled={busy}>
            <ArchiveIcon />
            backup
          </Button>
          <Button
            variant="outline"
            onClick={() => setRestoreOpen(true)}
            disabled={busy || selectedBackups.length !== 1}
          >
            <RotateCcwIcon />
            restore
          </Button>
          <Button variant="outline" onClick={() => void loadBackups()} disabled={busy || loading}>
            <RefreshCwIcon />
            refresh
          </Button>
          <Button
            variant="destructive"
            onClick={() => setDeleteOpen(true)}
            disabled={busy || selectedBackups.length === 0}
          >
            <Trash2Icon />
            delete
          </Button>
          <Button variant="ghost" onClick={() => void openActivityLog()} disabled={busy}>
            <FileClockIcon />
            log
          </Button>
        </div>
        <div className="text-xs text-muted-foreground">
          {selectedBackups.length} selected / {sortedBackups.length} backups
        </div>
      </div>

      <div className="min-h-0 flex-1 overflow-auto">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-10">
                <input
                  type="checkbox"
                  aria-label="Select all backups"
                  checked={sortedBackups.length > 0 && selectedNames.size === sortedBackups.length}
                  onChange={toggleAllBackups}
                />
              </TableHead>
              <TableHead>name</TableHead>
              <TableHead>modified</TableHead>
              <TableHead className="text-right">size</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {sortedBackups.map((backup) => {
              const selected = selectedNames.has(backup.name);
              return (
                <TableRow
                  key={backup.name}
                  data-state={selected ? 'selected' : undefined}
                  onClick={() => toggleBackup(backup.name)}
                  className="cursor-default"
                >
                  <TableCell onClick={(event) => event.stopPropagation()}>
                    <input
                      type="checkbox"
                      aria-label={`Select ${backup.name}`}
                      checked={selected}
                      onChange={() => toggleBackup(backup.name)}
                    />
                  </TableCell>
                  <TableCell className="font-medium">{backup.name}</TableCell>
                  <TableCell>{formatDate(backup.lastModified)}</TableCell>
                  <TableCell className="text-right tabular-nums">{formatBytes(backup.size)}</TableCell>
                </TableRow>
              );
            })}
            {!loading && sortedBackups.length === 0 && (
              <TableRow>
                <TableCell colSpan={4} className="h-32 text-center text-muted-foreground">
                  No backups found.
                </TableCell>
              </TableRow>
            )}
            {loading && (
              <TableRow>
                <TableCell colSpan={4} className="h-32 text-center text-muted-foreground">
                  Loading backups...
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create Backup</DialogTitle>
          </DialogHeader>
          <div className="grid gap-2">
            <Label htmlFor="backup-name">name</Label>
            <Input
              id="backup-name"
              value={backupName}
              onChange={(event) => setBackupName(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') void createBackup();
              }}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCreateOpen(false)} disabled={busy}>
              cancel
            </Button>
            <Button onClick={() => void createBackup()} disabled={busy}>
              create
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={restoreOpen} onOpenChange={setRestoreOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Restore backup?</AlertDialogTitle>
            <AlertDialogDescription>
              Restore {selectedBackups[0]?.name}. Create a new backup before restoring?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={busy}>cancel</AlertDialogCancel>
            <AlertDialogAction variant="outline" disabled={busy} onClick={() => void restoreBackup(false)}>
              no
            </AlertDialogAction>
            <AlertDialogAction disabled={busy} onClick={() => void restoreBackup(true)}>
              yes
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <AlertDialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete backups?</AlertDialogTitle>
            <AlertDialogDescription>
              Delete {selectedBackups.length} selected backup{selectedBackups.length === 1 ? '' : 's'}.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={busy}>cancel</AlertDialogCancel>
            <AlertDialogAction variant="destructive" disabled={busy} onClick={() => void deleteBackups()}>
              delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      <Dialog open={activityOpen} onOpenChange={setActivityOpen}>
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>Activity Log</DialogTitle>
          </DialogHeader>
          <div className="max-h-[60vh] overflow-auto border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>date</TableHead>
                  <TableHead>action</TableHead>
                  <TableHead>filename</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {activityLog.map((activity) => (
                  <TableRow key={`${activity.dateTime}-${activity.activityType}-${activity.fileName}`}>
                    <TableCell>{formatDate(activity.dateTime)}</TableCell>
                    <TableCell>{activity.activityType}</TableCell>
                    <TableCell>{activity.fileName}</TableCell>
                  </TableRow>
                ))}
                {activityLog.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={3} className="h-24 text-center text-muted-foreground">
                      No activity recorded.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setActivityOpen(false)}>
              close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function defaultBackupName() {
  const now = new Date();
  const pad = (value: number) => value.toString().padStart(2, '0');
  return `save_${now.getFullYear()}_${pad(now.getMonth() + 1)}_${pad(now.getDate())}_${pad(now.getHours())}_${pad(now.getMinutes())}`;
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(date);
}

function formatBytes(value: number) {
  if (value < 1024) return `${value} B`;
  const units = ['KB', 'MB', 'GB', 'TB'];
  let size = value / 1024;
  let unitIndex = 0;

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024;
    unitIndex += 1;
  }

  return `${size.toFixed(size >= 10 ? 1 : 2)} ${units[unitIndex]}`;
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Unknown error';
}

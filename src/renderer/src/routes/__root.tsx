import { useEffect, useState } from 'react';
import { createRootRoute, Link, Outlet } from '@tanstack/react-router';
import { Toaster } from '@renderer/components/ui/sonner';
import { Separator } from '@renderer/components/ui/separator';
import { TooltipProvider } from '@renderer/components/ui/tooltip';
import type { MemoryStatus } from '../../../shared/types';

const tabs = [
  { to: '/', label: 'saves' },
  { to: '/salakieli', label: 'salakieli' },
  { to: '/quickref', label: 'quickref', disabled: true },
  { to: '/bone-wands', label: 'bone wands', disabled: true },
];

const RootLayout = () => {
  const [memoryStatus, setMemoryStatus] = useState<MemoryStatus | null>(null);

  useEffect(() => {
    let unsubscribe: (() => void) | undefined;
    let mounted = true;

    window.noitaUtil.noitaProcess
      .getMemoryStatus()
      .then((status) => {
        if (mounted) setMemoryStatus(status);
      })
      .catch(() => {
        if (mounted) {
          setMemoryStatus({
            message: 'looking for noita process...',
            processFound: false,
            overWarning: false,
          });
        }
      });

    window.noitaUtil.noitaProcess
      .startMemoryMonitor()
      .then(() => {
        if (!mounted) return;
        unsubscribe = window.noitaUtil.noitaProcess.onMemoryStatus(setMemoryStatus);
      })
      .catch(() => undefined);

    return () => {
      mounted = false;
      unsubscribe?.();
      void window.noitaUtil.noitaProcess.stopMemoryMonitor();
    };
  }, []);

  return (
    <TooltipProvider>
      <div className="flex h-screen flex-col bg-background text-foreground">
        <header className="flex h-12 items-center justify-between border-b px-4">
          <nav className="flex items-center gap-1 text-xs">
            {tabs.map((tab) =>
              tab.disabled ? (
                <span key={tab.to} className="rounded-md px-3 py-1.5 text-muted-foreground/60">
                  {tab.label}
                </span>
              ) : (
                <Link
                  key={tab.to}
                  to={tab.to}
                  className="rounded-md px-3 py-1.5 text-muted-foreground hover:bg-muted hover:text-foreground"
                  activeProps={{ className: 'bg-muted text-foreground' }}
                >
                  {tab.label}
                </Link>
              ),
            )}
          </nav>
          <div className="text-xs text-muted-foreground">noita-util</div>
        </header>

        <main className="min-h-0 flex-1 overflow-hidden">
          <Outlet />
        </main>

        <Separator />
        <footer className="flex h-7 items-center px-4 text-xs text-muted-foreground">
          {memoryStatus?.message ?? 'looking for noita process...'}
        </footer>
      </div>
      <Toaster />
    </TooltipProvider>
  );
};

export const Route = createRootRoute({ component: RootLayout });

import { useEffect } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { createRootRoute, Link, Outlet } from "@tanstack/react-router";
import { SettingsIcon } from "lucide-react";
import { Toaster } from "@renderer/components/ui/sonner";
import { Separator } from "@renderer/components/ui/separator";
import { TooltipProvider } from "@renderer/components/ui/tooltip";
import type { MemoryStatus } from "../../../shared/types";

const tabs: Array<{ to: string; label: string; disabled?: boolean }> = [
  { to: "/", label: "saves" },
  { to: "/salakieli", label: "salakieli" },
  { to: "/quickref", label: "quickref" },
  { to: "/bone-wands", label: "bone wands" },
];

const memoryStatusQueryKey = ["noitaProcess", "memoryStatus"];

const lookingForNoitaStatus: MemoryStatus = {
  message: "looking for noita process...",
  processFound: false,
  overWarning: false,
};

const RootLayout = () => {
  const queryClient = useQueryClient();
  const memoryStatusQuery = useQuery({
    queryKey: memoryStatusQueryKey,
    queryFn: async () => {
      try {
        return await window.noitaUtil.noitaProcess.getMemoryStatus();
      } catch {
        return lookingForNoitaStatus;
      }
    },
  });

  useEffect(() => {
    return window.noitaUtil.noitaProcess.onMemoryStatus((status) => {
      queryClient.setQueryData(memoryStatusQueryKey, status);
    });
  }, [queryClient]);

  return (
    <TooltipProvider>
      <div className="flex h-screen flex-col bg-background text-foreground">
        <header className="flex h-12 items-center justify-between border-b px-4">
          <nav className="flex items-center gap-1 text-xs">
            {tabs.map((tab) =>
              tab.disabled ? (
                <span
                  key={tab.to}
                  className="rounded-md px-3 py-1.5 text-muted-foreground/60"
                >
                  {tab.label}
                </span>
              ) : (
                <Link
                  key={tab.to}
                  to={tab.to}
                  className="rounded-md px-3 py-1.5 text-muted-foreground hover:bg-muted hover:text-foreground"
                  activeProps={{ className: "bg-muted text-foreground" }}
                >
                  {tab.label}
                </Link>
              ),
            )}
          </nav>
          <div className="flex items-center gap-3">
            <div className="text-xs text-muted-foreground">noita-util</div>
            <Link
              to="/settings"
              aria-label="settings"
              className="inline-flex items-center gap-1 rounded-md px-3 py-1.5 text-xs text-muted-foreground hover:bg-muted hover:text-foreground [&_svg]:size-3"
              activeProps={{ className: "bg-muted text-foreground" }}
            >
              <SettingsIcon />
              settings
            </Link>
          </div>
        </header>

        <main className="min-h-0 flex-1 overflow-hidden p-4">
          <Outlet />
        </main>

        <Separator />
        <footer className="flex h-7 items-center px-4 text-xs text-muted-foreground">
          {memoryStatusQuery.data?.message ?? lookingForNoitaStatus.message}
        </footer>
      </div>
      <Toaster />
    </TooltipProvider>
  );
};

export const Route = createRootRoute({ component: RootLayout });

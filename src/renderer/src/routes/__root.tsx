import { createRootRoute, Outlet } from '@tanstack/react-router';
import { Toaster } from '@renderer/components/ui/sonner';
import { TooltipProvider } from '@renderer/components/ui/tooltip';

const RootLayout = () => (
  <TooltipProvider>
    <div className="p-2 flex gap-2"></div>
    <hr />

    <Outlet />
    <Toaster />
  </TooltipProvider>
);

export const Route = createRootRoute({ component: RootLayout });

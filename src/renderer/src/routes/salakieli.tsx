import { useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useRef, useState } from "react";
import type { AppConfig } from "../../../shared/types";

import { Button } from "@renderer/components/ui/button";
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@renderer/components/ui/resizable";
import { Textarea } from "@renderer/components/ui/textarea";
import { cn } from "@renderer/lib/utils";

export const Route = createFileRoute("/salakieli")({
  component: SalakieliPage,
});

const EMPTY_FILES: Record<string, string> = {};
const LEFT_PANEL_MIN = 200;
const LEFT_PANEL_MAX = 250;

function SalakieliPage() {
  const queryClient = useQueryClient();
  const [selectedFile, setSelectedFile] = useState<string | null>(null);
  const configQuery = useQuery({
    queryKey: ["config"],
    queryFn: () => window.noitaUtil.config.load(),
  });
  const salakieliQuery = useQuery({
    queryKey: ["salakieli", "decryptAll"],
    queryFn: () => window.noitaUtil.salakieli.decryptAll(),
  });

  const persistedSize = configQuery.data?.salakieliSplitterPosition ?? -1;
  const defaultSize =
    persistedSize > 0
      ? Math.min(Math.max(persistedSize, LEFT_PANEL_MIN), LEFT_PANEL_MAX)
      : LEFT_PANEL_MIN;

  const configRef = useRef<AppConfig | undefined>(undefined);
  configRef.current = configQuery.data;

  const saveTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const handleResize = (size: unknown) => {
    const num = Number(size);
    if (Number.isNaN(num)) return;
    if (saveTimeoutRef.current) clearTimeout(saveTimeoutRef.current);
    saveTimeoutRef.current = setTimeout(() => {
      const current = configRef.current;
      if (!current) return;
      const clamped = Math.min(
        Math.max(Math.round(num), LEFT_PANEL_MIN),
        LEFT_PANEL_MAX,
      );
      const updated: AppConfig = {
        ...current,
        salakieliSplitterPosition: clamped,
      };
      window.noitaUtil.config.save(updated).then((saved) => {
        queryClient.setQueryData(["config"], saved);
      });
    }, 300);
  };

  const files = salakieliQuery.data ?? EMPTY_FILES;
  const fileNames = useMemo(() => Object.keys(files).sort(), [files]);
  const activeFile =
    selectedFile && fileNames.includes(selectedFile)
      ? selectedFile
      : (fileNames[0] ?? null);
  const selectedText = activeFile ? files[activeFile] : "";

  return (
    <div className="h-full">
      <ResizablePanelGroup orientation="horizontal">
        <ResizablePanel
          defaultSize={defaultSize}
          minSize={LEFT_PANEL_MIN}
          maxSize={LEFT_PANEL_MAX}
          onResize={handleResize}
        >
          <aside className="flex h-full flex-col border-r">
            <div className="flex h-12 items-center border-b px-4 text-xs font-medium">
              salakieli
            </div>
            <div className="min-h-0 flex-1 overflow-auto p-2">
              {fileNames.map((fileName) => (
                <Button
                  key={fileName}
                  variant="ghost"
                  className={cn(
                    "mb-1 h-8 w-full justify-start px-2 font-normal",
                    activeFile === fileName && "bg-muted text-foreground",
                  )}
                  onClick={() => setSelectedFile(fileName)}
                >
                  {fileName}
                </Button>
              ))}

              {salakieliQuery.isPending && (
                <div className="px-2 py-3 text-xs text-muted-foreground">
                  decrypting...
                </div>
              )}
              {salakieliQuery.isError && (
                <div className="px-2 py-3 text-xs text-destructive">
                  {getErrorMessage(salakieliQuery.error)}
                </div>
              )}
              {salakieliQuery.isSuccess && fileNames.length === 0 && (
                <div className="px-2 py-3 text-xs text-muted-foreground">
                  No files decrypted.
                </div>
              )}
            </div>
          </aside>
        </ResizablePanel>

        <ResizableHandle />

        <ResizablePanel minSize={35}>
          <section className="flex h-full flex-col">
            <div className="flex h-12 items-center justify-between border-b px-4">
              <div className="text-xs font-medium">
                {activeFile ?? "No file selected"}
              </div>
              {selectedText && (
                <div className="text-xs text-muted-foreground tabular-nums">
                  {selectedText.length.toLocaleString()} chars
                </div>
              )}
            </div>
            <div className="min-h-0 flex-1 p-3">
              <Textarea
                readOnly
                value={selectedText ?? ""}
                className="h-full min-h-full resize-none overflow-auto font-mono text-xs leading-relaxed"
                placeholder={
                  salakieliQuery.isPending
                    ? "Decrypting files..."
                    : "Select a file."
                }
              />
            </div>
          </section>
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Unknown error";
}

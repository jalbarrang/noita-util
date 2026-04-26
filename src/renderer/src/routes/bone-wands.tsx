import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";

import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@renderer/components/ui/resizable";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@renderer/components/ui/table";
import type { BoneWand, Spell, Wand } from "../../../shared/types";

export const Route = createFileRoute("/bone-wands")({
  component: BoneWandsPage,
});

const EMPTY_BONE_WANDS: BoneWand[] = [];

const wandStats: Array<{ label: string; getValue: (wand: Wand) => string }> = [
  { label: "Shuffle", getValue: (wand) => (wand.shuffle ? "Yes" : "No") },
  { label: "Spells/Cast", getValue: (wand) => formatNumber(wand.spellsCast) },
  {
    label: "Cast delay",
    getValue: (wand) => `${formatNumber(wand.castDelay)} s`,
  },
  {
    label: "Rechrg. Time",
    getValue: (wand) => `${formatNumber(wand.rechargeTime)} s`,
  },
  { label: "Mana max", getValue: (wand) => formatNumber(wand.manaMax) },
  {
    label: "Mana chg. Spd",
    getValue: (wand) => formatNumber(wand.manaChargeSpeed),
  },
  { label: "Capacity", getValue: (wand) => formatNumber(wand.capacity) },
  { label: "Spread", getValue: (wand) => `${formatNumber(wand.spread)} DEG` },
];

function BoneWandsPage() {
  const [selectedFileName, setSelectedFileName] = useState<string | null>(null);
  const noitaDataQuery = useQuery({
    queryKey: ["noitaData", "load"],
    queryFn: () => window.noitaUtil.noitaData.load(),
  });

  const boneWands = noitaDataQuery.data?.boneWands ?? EMPTY_BONE_WANDS;
  const sortedBoneWands = useMemo(
    () =>
      [...boneWands].sort(
        (left, right) =>
          Date.parse(right.lastModified) - Date.parse(left.lastModified),
      ),
    [boneWands],
  );
  const selectedBoneWand = useMemo(() => {
    const selected = selectedFileName
      ? sortedBoneWands.find(
          (boneWand) => boneWand.fileName === selectedFileName,
        )
      : undefined;
    return selected ?? sortedBoneWands[0] ?? null;
  }, [selectedFileName, sortedBoneWands]);

  return (
    <div className="h-full">
      <ResizablePanelGroup orientation="horizontal">
        <ResizablePanel defaultSize={300} minSize={300} maxSize={400}>
          <section className="flex h-full flex-col border-r">
            <div className="flex h-12 items-center justify-between border-b px-4">
              <div className="text-xs font-medium">bone wands</div>
              <div className="text-xs text-muted-foreground tabular-nums">
                {sortedBoneWands.length}
              </div>
            </div>

            <div className="min-h-0 flex-1 overflow-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>filename</TableHead>
                    <TableHead>modified</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sortedBoneWands.map((boneWand) => {
                    const selected =
                      selectedBoneWand?.fileName === boneWand.fileName;
                    return (
                      <TableRow
                        key={boneWand.fileName}
                        data-state={selected ? "selected" : undefined}
                        onClick={() => setSelectedFileName(boneWand.fileName)}
                        className="cursor-default"
                      >
                        <TableCell className="font-medium">
                          {boneWand.fileName}
                        </TableCell>
                        <TableCell>
                          {formatDate(boneWand.lastModified)}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                  {noitaDataQuery.isPending && (
                    <TableRow>
                      <TableCell
                        colSpan={2}
                        className="h-32 text-center text-muted-foreground"
                      >
                        Loading bone wands...
                      </TableCell>
                    </TableRow>
                  )}
                  {noitaDataQuery.isError && (
                    <TableRow>
                      <TableCell
                        colSpan={2}
                        className="h-32 text-center text-destructive"
                      >
                        {getErrorMessage(noitaDataQuery.error)}
                      </TableCell>
                    </TableRow>
                  )}
                  {noitaDataQuery.isSuccess && sortedBoneWands.length === 0 && (
                    <TableRow>
                      <TableCell
                        colSpan={2}
                        className="h-32 text-center text-muted-foreground"
                      >
                        No bone wands found.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>
          </section>
        </ResizablePanel>
        <ResizableHandle />
        <ResizablePanel minSize={35}>
          <WandDetails boneWand={selectedBoneWand} />
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
}

function WandDetails({ boneWand }: { boneWand: BoneWand | null }) {
  if (!boneWand) {
    return (
      <aside className="flex h-full flex-col">
        <div className="flex h-12 items-center border-b px-4 text-xs font-medium">
          No wand selected
        </div>
        <div className="p-4 text-xs text-muted-foreground">
          Select a bone wand.
        </div>
      </aside>
    );
  }

  const wand = boneWand.wand;

  return (
    <aside className="flex h-full flex-col">
      <div className="flex h-12 items-center justify-between border-b px-4">
        <div className="truncate text-xs font-medium">{boneWand.fileName}</div>
        <div className="text-xs text-muted-foreground">
          {formatDate(boneWand.lastModified)}
        </div>
      </div>

      <div className="min-h-0 flex-1 overflow-auto p-4">
        <div className="grid gap-5">
          <section className="grid gap-3">
            <div className="text-xs font-medium">wand</div>
            <div className="flex min-h-28 items-center justify-center rounded-md border bg-muted/30 p-3">
              {wand.spriteFileUrl ? (
                <img
                  src={wand.spriteFileUrl}
                  alt={boneWand.fileName}
                  title={wand.spriteFile}
                  className="max-h-24 w-auto object-contain"
                />
              ) : (
                <div className="text-xs text-muted-foreground">
                  No wand image
                </div>
              )}
            </div>
          </section>

          <section className="grid gap-2">
            <div className="text-xs font-medium">stats</div>
            <div className="grid max-w-lg grid-cols-[10rem_1fr] border text-xs">
              {wandStats.map((stat) => (
                <StatRow
                  key={stat.label}
                  label={stat.label}
                  value={stat.getValue(wand)}
                />
              ))}
            </div>
          </section>

          <SpellSection title="always casts" spells={wand.alwaysCasts} />
          <SpellSection
            title="spells"
            spells={wand.spells}
            capacity={wand.capacity}
          />
        </div>
      </div>
    </aside>
  );
}

function StatRow({ label, value }: { label: string; value: string }) {
  return (
    <>
      <div className="border-r border-b px-2 py-1.5 text-muted-foreground">
        {label}
      </div>
      <div className="border-b px-2 py-1.5 tabular-nums">{value}</div>
    </>
  );
}

function SpellSection({
  title,
  spells,
  capacity,
}: {
  title: string;
  spells: Spell[];
  capacity?: number;
}) {
  const slotCount =
    capacity === undefined ? spells.length : Math.max(capacity, spells.length);

  return (
    <section className="grid gap-2">
      <div className="flex items-center justify-between">
        <div className="text-xs font-medium">{title}</div>
        <div className="text-xs text-muted-foreground tabular-nums">
          {spells.length}
        </div>
      </div>

      {slotCount > 0 ? (
        <div className="grid grid-cols-[repeat(auto-fill,minmax(2.25rem,2.25rem))] gap-2">
          {Array.from({ length: slotCount }, (_, index) => (
            <SpellSlot key={index} spell={spells[index]} />
          ))}
        </div>
      ) : (
        <div className="text-xs text-muted-foreground">None.</div>
      )}
    </section>
  );
}

function SpellSlot({ spell }: { spell: Spell | undefined }) {
  if (!spell) {
    return <div className="size-9 rounded-md border bg-muted/20" />;
  }

  const imageUrl =
    spell.uiImageUrl || spell.spriteUrl || spell.spriteUnidentifiedUrl;
  const label = spell.english_name || spell.name || spell.id;

  return (
    <div
      title={label}
      className="flex size-9 items-center justify-center overflow-hidden rounded-md border bg-muted/30"
    >
      {imageUrl ? (
        <img
          src={imageUrl}
          alt={label}
          className="max-h-8 w-auto object-contain"
        />
      ) : (
        <div className="px-1 text-center text-[9px] leading-tight text-muted-foreground">
          {spell.id}
        </div>
      )}
    </div>
  );
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  }).format(date);
}

function formatNumber(value: number) {
  return Number.isFinite(value) ? value.toLocaleString() : "-";
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Unknown error";
}

import { useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useDeferredValue, useMemo, useRef, useState } from "react";
import type { AppConfig } from "../../../shared/types";

import { Input } from "@renderer/components/ui/input";
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@renderer/components/ui/resizable";

import { cn } from "@renderer/lib/utils";
import type { Spell } from "../../../shared/types";

export const Route = createFileRoute("/quickref")({
  component: QuickrefPage,
});

const EMPTY_SPELLS: Spell[] = [];
const LEFT_PANEL_MIN = 300;
const LEFT_PANEL_MAX = 500;

const detailFields: Array<{
  label: string;
  getValue: (spell: Spell) => string;
}> = [
  { label: "id", getValue: (spell) => spell.id },
  { label: "name", getValue: (spell) => spell.name },
  { label: "description", getValue: (spell) => spell.description },
  { label: "english_name", getValue: (spell) => spell.english_name },
  { label: "english_desc", getValue: (spell) => spell.english_desc },
  { label: "type", getValue: (spell) => spell.type },
  { label: "spawn_level", getValue: (spell) => spell.spawn_level.join(", ") },
  {
    label: "spawn_probability",
    getValue: (spell) => spell.spawn_probability.join(", "),
  },
  { label: "price", getValue: (spell) => formatNumber(spell.price) },
  { label: "mana", getValue: (spell) => formatNumber(spell.mana) },
  { label: "max_uses", getValue: (spell) => formatNumber(spell.max_uses) },
  {
    label: "never_unlimited",
    getValue: (spell) => formatBoolean(spell.never_unlimited),
  },
  {
    label: "spawn_manual_unlock",
    getValue: (spell) => formatBoolean(spell.spawn_manual_unlock),
  },
  { label: "recursive", getValue: (spell) => formatBoolean(spell.recursive) },
  {
    label: "ai_never_uses",
    getValue: (spell) => formatBoolean(spell.ai_never_uses),
  },
  {
    label: "is_dangerous_blast",
    getValue: (spell) => formatBoolean(spell.is_dangerous_blast),
  },
  { label: "sprite", getValue: (spell) => spell.sprite },
  { label: "spriteUrl", getValue: (spell) => spell.spriteUrl },
  {
    label: "sprite_unidentified",
    getValue: (spell) => spell.sprite_unidentified,
  },
  {
    label: "spriteUnidentifiedUrl",
    getValue: (spell) => spell.spriteUnidentifiedUrl,
  },
  {
    label: "related_projectiles",
    getValue: (spell) => spell.related_projectiles,
  },
  { label: "custom_xml_file", getValue: (spell) => spell.custom_xml_file },
  {
    label: "spawn_requires_flag",
    getValue: (spell) => spell.spawn_requires_flag,
  },
  { label: "sound_loop_tag", getValue: (spell) => spell.sound_loop_tag },
  {
    label: "related_extra_entities",
    getValue: (spell) => spell.related_extra_entities,
  },
  { label: "uiImageFilename", getValue: (spell) => spell.uiImageFilename },
  { label: "uiImageUrl", getValue: (spell) => spell.uiImageUrl },
];

function QuickrefPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const deferredSearch = useDeferredValue(search);
  const [selectedSpellId, setSelectedSpellId] = useState<string | null>(null);

  const configQuery = useQuery({
    queryKey: ["config"],
    queryFn: () => window.noitaUtil.config.load(),
  });

  const noitaDataQuery = useQuery({
    queryKey: ["noitaData", "load"],
    queryFn: () => window.noitaUtil.noitaData.load(),
  });

  const persistedSize = configQuery.data?.qrefSplitterPosition ?? -1;
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
        qrefSplitterPosition: clamped,
      };
      window.noitaUtil.config.save(updated).then((saved) => {
        queryClient.setQueryData(["config"], saved);
      });
    }, 300);
  };

  const spells = noitaDataQuery.data?.spells ?? EMPTY_SPELLS;
  const filteredSpells = useMemo(
    () => filterSpells(spells, deferredSearch),
    [deferredSearch, spells],
  );

  const selectedSpell = useMemo(() => {
    const selected = selectedSpellId
      ? filteredSpells.find((spell) => spell.id === selectedSpellId)
      : undefined;
    return selected ?? filteredSpells[0] ?? null;
  }, [filteredSpells, selectedSpellId]);

  const spellImageUrl =
    selectedSpell?.uiImageUrl ||
    selectedSpell?.spriteUrl ||
    selectedSpell?.spriteUnidentifiedUrl ||
    "";

  return (
    <div className="h-full">
      <ResizablePanelGroup orientation="horizontal">
        <ResizablePanel
          defaultSize={defaultSize}
          minSize={LEFT_PANEL_MIN}
          maxSize={LEFT_PANEL_MAX}
          onResize={handleResize}
        >
          <section className="flex h-full flex-col border-r">
            <div className="flex h-12 items-center gap-3 border-b px-4">
              <div className="text-xs font-medium">quickref</div>
              <Input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="search spells"
                className="max-w-sm"
              />
              <div className="ml-auto text-xs text-muted-foreground tabular-nums">
                {filteredSpells.length} / {spells.length}
              </div>
            </div>

            <div className="min-h-0 flex-1 overflow-auto">
              <div className="flex flex-col">
                {filteredSpells.map((spell) => {
                  const selected = selectedSpell?.id === spell.id;
                  return (
                    <div
                      key={spell.id}
                      data-state={selected ? "selected" : undefined}
                      onClick={() => setSelectedSpellId(spell.id)}
                      className={cn(
                        "flex cursor-default items-start gap-3 border-b px-3 py-2 transition-colors",
                        "hover:bg-muted/50",
                        selected && "bg-muted",
                      )}
                    >
                      <div className="shrink-0 pt-0.5">
                        <SpellImage spell={spell} />
                      </div>
                      <div className="flex min-w-0 flex-col gap-0.5">
                        <div className="truncate text-sm font-medium">
                          {spell.english_name || spell.name}
                        </div>
                        <div className="truncate font-mono text-[11px] text-muted-foreground">
                          {spell.id}
                        </div>
                        <div className="truncate text-xs text-muted-foreground">
                          {spell.english_desc || spell.description}
                        </div>
                      </div>
                    </div>
                  );
                })}
                {noitaDataQuery.isPending && (
                  <div className="flex h-32 items-center justify-center text-sm text-muted-foreground">
                    Loading spells...
                  </div>
                )}
                {noitaDataQuery.isError && (
                  <div className="flex h-32 items-center justify-center text-sm text-destructive">
                    {getErrorMessage(noitaDataQuery.error)}
                  </div>
                )}
                {noitaDataQuery.isSuccess && filteredSpells.length === 0 && (
                  <div className="flex h-32 items-center justify-center text-sm text-muted-foreground">
                    No spells found.
                  </div>
                )}
              </div>
            </div>
          </section>
        </ResizablePanel>
        <ResizableHandle />
        <ResizablePanel minSize={28}>
          <SpellDetails spell={selectedSpell} imageUrl={spellImageUrl} />
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
}

function SpellDetails({
  spell,
  imageUrl,
}: {
  spell: Spell | null;
  imageUrl: string;
}) {
  return (
    <aside className="flex h-full flex-col">
      <div className="flex h-12 items-center justify-between border-b px-4">
        <div className="truncate text-xs font-medium">
          {spell?.english_name ?? "No spell selected"}
        </div>
        {spell && (
          <div className="text-xs text-muted-foreground">{spell.type}</div>
        )}
      </div>

      <div className="min-h-0 flex-1 overflow-auto p-4">
        {!spell && (
          <div className="text-xs text-muted-foreground">Select a spell.</div>
        )}

        {spell && (
          <div className="grid gap-3">
            <div className="grid gap-1">
              <div className="text-xs font-medium">image</div>
              <div className="flex min-h-20 items-center justify-center rounded-md border bg-muted/30 p-3">
                {imageUrl ? (
                  <img
                    src={imageUrl}
                    alt={spell.english_name || spell.name}
                    className="max-h-16 w-auto object-contain"
                  />
                ) : (
                  <div className="text-xs text-muted-foreground">No image</div>
                )}
              </div>
            </div>
            {detailFields.map((field) => (
              <DetailRow
                key={field.label}
                label={field.label}
                value={field.getValue(spell)}
              />
            ))}
            <div className="grid gap-1">
              <div className="text-xs font-medium">action</div>
              <pre className="max-h-80 overflow-auto rounded-md border bg-muted/30 p-3 text-xs leading-relaxed whitespace-pre-wrap">
                {spell.action.length > 0 ? spell.action.join("\n") : "-"}
              </pre>
            </div>
          </div>
        )}
      </div>
    </aside>
  );
}

function SpellImage({ spell }: { spell: Spell }) {
  const imageUrl =
    spell.uiImageUrl || spell.spriteUrl || spell.spriteUnidentifiedUrl;

  if (!imageUrl) {
    return (
      <div className="flex size-8 items-center justify-center rounded-md border bg-muted text-[10px] text-muted-foreground">
        img
      </div>
    );
  }

  return (
    <div className="flex size-8 items-center justify-center overflow-hidden rounded-md border bg-muted/30">
      <img
        src={imageUrl}
        alt={spell.english_name || spell.name}
        title={spell.sprite || spell.uiImageFilename || "Spell image"}
        className="max-h-7 w-auto object-contain"
      />
    </div>
  );
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid gap-1 border-b pb-2 last:border-b-0">
      <div className="text-xs font-medium">{label}</div>
      <div
        className={cn(
          "text-xs leading-relaxed text-muted-foreground",
          value && "text-foreground",
        )}
      >
        {value || "-"}
      </div>
    </div>
  );
}

function filterSpells(spells: Spell[], search: string) {
  const query = search.trim().toLocaleLowerCase();
  if (!query) return spells;

  return spells.filter((spell) =>
    [
      spell.id,
      spell.name,
      spell.description,
      spell.english_name,
      spell.english_desc,
    ]
      .join(" ")
      .toLocaleLowerCase()
      .includes(query),
  );
}

function formatBoolean(value: boolean) {
  return value ? "true" : "false";
}

function formatNumber(value: number) {
  return Number.isFinite(value) ? value.toLocaleString() : "-";
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Unknown error";
}

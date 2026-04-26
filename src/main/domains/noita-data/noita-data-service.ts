import { parse as parseCsv } from 'csv-parse/sync';
import { XMLParser } from 'fast-xml-parser';
import { readdir, readFile, stat } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import type {
  ActionType,
  AppConfig,
  BoneWand,
  NoitaData,
  Perk,
  Spell,
  Wand,
} from '../../../shared/types.js';
import { loadConfig } from '../config/config-service.js';
import {
  cleanup,
  filterLuaComments,
  getRightOfEqual,
  groupWithSep,
  secondsFromFrames,
  toIntViaFloat,
} from './parser-utils.js';

const defaultSpell = (): Spell => ({
  id: '',
  name: '',
  description: '',
  english_name: '',
  english_desc: '',
  type: 'ACTION_TYPE_OTHER',
  spawn_level: [],
  spawn_probability: [],
  price: 0,
  mana: 0,
  max_uses: 0,
  never_unlimited: false,
  spawn_manual_unlock: false,
  recursive: false,
  ai_never_uses: false,
  is_dangerous_blast: false,
  action: [],
  sprite: '',
  sprite_unidentified: '',
  related_projectiles: '',
  custom_xml_file: '',
  spawn_requires_flag: '',
  sound_loop_tag: '',
  related_extra_entities: '',
  uiImageFilename: '',
});

function translationsFile(config: AppConfig): string {
  return join(dirname(config.noitaExeFile), 'data', 'translations', 'common.csv');
}

function gunActionsFile(config: AppConfig): string {
  return join(config.noitaSaveFolder, 'data', 'scripts', 'gun', 'gun_actions.lua');
}

function perkListFile(config: AppConfig): string {
  return join(config.noitaSaveFolder, 'data', 'scripts', 'perks', 'perk_list.lua');
}

async function assertFile(path: string, label: string): Promise<string> {
  const info = await stat(path).catch(() => null);
  if (!info?.isFile()) {
    throw new Error(`${label} file does not exist: ${path}`);
  }
  return path;
}

export async function loadTranslations(config?: AppConfig): Promise<Record<string, string>> {
  const appConfig = config ?? (await loadConfig());
  const file = await assertFile(translationsFile(appConfig), 'translations');
  const records = parseCsv(await readFile(file, 'utf8')) as string[][];
  return Object.fromEntries(
    records.map((record) => [`$${record[0]?.trim() ?? ''}`, record[1]?.trim() ?? '']),
  );
}

export async function loadSpells(
  config?: AppConfig,
  translations?: Record<string, string>,
): Promise<Spell[]> {
  const appConfig = config ?? (await loadConfig());
  const translationMap = translations ?? (await loadTranslations(appConfig));
  const file = await assertFile(gunActionsFile(appConfig), 'spells');
  const re = /\s+/g;
  const lines = filterLuaComments((await readFile(file, 'utf8')).split(/\r?\n/));
  const chunks = groupWithSep(lines, (line) => line.trim() === '},').map((group) =>
    group.map((line) => line.replace(re, ' ')),
  );

  let inFunc = false;
  const spells = chunks.map((chunk) => {
    const spell = defaultSpell();
    for (const line of chunk) {
      if (inFunc) {
        if (line.trim() === 'end,') {
          inFunc = false;
        }
        spell.action.push(line);
        continue;
      }

      if (!line.includes('=')) {
        continue;
      }

      const [rawProp, ...rest] = line.split('=');
      const prop = rawProp?.trim();
      const value = cleanup(rest.join('=').trim());

      switch (prop) {
        case 'id':
          spell.id = value;
          break;
        case 'name':
          spell.name = value;
          spell.english_name = translationMap[value] ?? spell.name;
          break;
        case 'description':
          spell.description = value;
          spell.english_desc = translationMap[value] ?? spell.description;
          break;
        case 'type':
          spell.type = value as ActionType;
          break;
        case 'sprite':
          spell.sprite = value;
          break;
        case 'sprite_unidentified':
          spell.sprite_unidentified = value;
          break;
        case 'related_projectiles':
          spell.related_projectiles = value;
          break;
        case 'spawn_level':
          spell.spawn_level = value
            .split(',')
            .filter(Boolean)
            .map((item) => Number.parseInt(item.trim(), 10));
          break;
        case 'spawn_probability':
          spell.spawn_probability = value
            .split(',')
            .filter(Boolean)
            .map((item) => Number.parseFloat(item.trim()));
          break;
        case 'price':
          spell.price = Number.parseInt(value, 10);
          break;
        case 'mana':
          spell.mana = Number.parseInt(value, 10);
          break;
        case 'max_uses':
          spell.max_uses = Number.parseInt(value, 10);
          break;
        case 'custom_xml_file':
          spell.custom_xml_file = value;
          break;
        case 'action':
          spell.action.push(value);
          inFunc = true;
          break;
        case 'never_unlimited':
        case 'spawn_manual_unlock':
        case 'recursive':
        case 'ai_never_uses':
        case 'is_dangerous_blast':
          spell[prop] = value === 'true';
          break;
        case 'spawn_requires_flag':
          spell.spawn_requires_flag = value;
          break;
        case 'sound_loop_tag':
          spell.sound_loop_tag = value;
          break;
        case 'related_extra_entities':
          spell.related_extra_entities = value;
          break;
      }
    }
    return spell;
  });

  return spells.sort((a, b) => a.english_name.localeCompare(b.english_name));
}

export async function loadPerks(
  config?: AppConfig,
  translations?: Record<string, string>,
): Promise<Perk[]> {
  const appConfig = config ?? (await loadConfig());
  const translationMap = translations ?? (await loadTranslations(appConfig));
  const file = await assertFile(perkListFile(appConfig), 'perks');
  const lines = filterLuaComments((await readFile(file, 'utf8')).split(/\r?\n/));
  const perks: Perk[] = [];
  let current: Perk = {
    id: '',
    ui_name: '',
    ui_description: '',
    ui_icon: '',
    perk_icon: '',
    english_name: '',
    english_desc: '',
  };

  for (const line of lines) {
    const trimmed = line.trim();
    if (trimmed === '},') {
      current.english_name = translationMap[current.ui_name] ?? current.ui_name;
      current.english_desc = translationMap[current.ui_description] ?? current.ui_description;
      perks.push(current);
      current = {
        id: '',
        ui_name: '',
        ui_description: '',
        ui_icon: '',
        perk_icon: '',
        english_name: '',
        english_desc: '',
      };
      continue;
    }

    if (trimmed.startsWith('id')) current.id = getRightOfEqual(trimmed);
    if (trimmed.startsWith('ui_name')) current.ui_name = getRightOfEqual(trimmed);
    if (trimmed.startsWith('ui_description')) current.ui_description = getRightOfEqual(trimmed);
    if (trimmed.startsWith('ui_icon')) current.ui_icon = getRightOfEqual(trimmed);
    if (trimmed.startsWith('perk_icon')) current.perk_icon = getRightOfEqual(trimmed);
  }

  return perks.sort((a, b) => a.english_name.localeCompare(b.english_name));
}

function asArray<T>(value: T | T[] | undefined): T[] {
  if (!value) return [];
  return Array.isArray(value) ? value : [value];
}

function attrs(node: unknown): Record<string, string> {
  return typeof node === 'object' && node !== null ? (node as Record<string, string>) : {};
}

function entities(root: Record<string, unknown>): Record<string, unknown>[] {
  return asArray(
    root['Entity'] as Record<string, unknown> | Record<string, unknown>[] | undefined,
  ).flatMap((entity) => [entity, ...entities(entity)]);
}

function loadSpellsForWand(
  root: Record<string, unknown>,
  spells: Map<string, Spell>,
  alwaysCastVal: string,
): Spell[] {
  return entities(root)
    .filter((entity) => String(entity['@_tags'] ?? '').includes('card_action'))
    .filter((entity) => attrs(entity['ItemComponent'])['@_permanently_attached'] === alwaysCastVal)
    .map((entity) => {
      const spellId = attrs(entity['ItemActionComponent'])['@_action_id'];
      const imageFilename = attrs(entity['SpriteComponent'])['@_image_file'];
      if (!spellId || !imageFilename) throw new Error('Wand spell component missing');
      const spell = spells.get(spellId);
      if (!spell) throw new Error(`Spell ${spellId} not found`);
      return { ...spell, uiImageFilename: imageFilename };
    });
}

export async function loadBoneWand(file: string, spells: Map<string, Spell>): Promise<BoneWand> {
  const parser = new XMLParser({ ignoreAttributes: false });
  const root = parser.parse(await readFile(file, 'utf8')) as Record<string, unknown>;
  const entity = attrs(root['Entity']);
  const ability = attrs((root['Entity'] as Record<string, unknown>)['AbilityComponent']);
  const gunConfig = attrs(
    ((root['Entity'] as Record<string, unknown>)['AbilityComponent'] as Record<string, unknown>)[
      'gun_config'
    ],
  );
  const gunactionConfig = attrs(
    ((root['Entity'] as Record<string, unknown>)['AbilityComponent'] as Record<string, unknown>)[
      'gunaction_config'
    ],
  );
  const info = await stat(file);
  const parsedRoot = root['Entity'] as Record<string, unknown>;
  const wand: Wand = {
    shuffle: gunConfig['@_shuffle_deck_when_empty'] === '1',
    spellsCast: toIntViaFloat(gunConfig['@_actions_per_round']),
    castDelay: secondsFromFrames(gunactionConfig['@_fire_rate_wait']),
    rechargeTime: secondsFromFrames(gunConfig['@_reload_time']),
    manaMax: toIntViaFloat(ability['@_mana_max']),
    manaChargeSpeed: toIntViaFloat(ability['@_mana_charge_speed']),
    capacity: toIntViaFloat(gunConfig['@_deck_capacity']),
    spread: Number(Number(gunactionConfig['@_spread_degrees'] ?? 0).toFixed(2)),
    alwaysCasts: loadSpellsForWand(parsedRoot, spells, '1'),
    spells: loadSpellsForWand(parsedRoot, spells, '0'),
    spriteFile: ability['@_sprite_file'] ?? entity['@_sprite_file'] ?? '',
  };

  return {
    fileName: file.split(/[\\/]/).at(-1) ?? file,
    lastModified: info.mtime.toISOString(),
    wand,
  };
}

export async function loadBoneWands(config?: AppConfig, spells?: Spell[]): Promise<BoneWand[]> {
  const appConfig = config ?? (await loadConfig());
  const spellList = spells ?? (await loadSpells(appConfig));
  const spellMap = new Map(spellList.map((spell) => [spell.id, spell]));
  const boneFolder = join(appConfig.noitaSaveFolder, 'save00', 'persistent', 'bones_new');
  const entries = await readdir(boneFolder, { withFileTypes: true }).catch(() => []);
  const wands = await Promise.all(
    entries
      .filter((entry) => entry.isFile() && entry.name.toLowerCase().endsWith('.xml'))
      .map((entry) => loadBoneWand(join(boneFolder, entry.name), spellMap).catch(() => null)),
  );
  return wands.filter((wand): wand is BoneWand => wand !== null);
}

export async function loadNoitaData(): Promise<NoitaData> {
  const config = await loadConfig();
  const translations = await loadTranslations(config);
  const spells = await loadSpells(config, translations);
  const perks = await loadPerks(config, translations);
  const boneWands = await loadBoneWands(config, spells);
  return { translations, spells, perks, boneWands };
}

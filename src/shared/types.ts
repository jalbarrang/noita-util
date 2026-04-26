export const APP_NAME = 'noita-util';

export type AppConfig = {
  winX: number;
  winY: number;
  winWidth: number;
  winHeight: number;
  noitaSaveFolder: string;
  noitaBackupFolder: string;
  noitaExeFile: string;
  salakieliSplitterPosition: number;
  qrefSplitterPosition: number;
  spellTableColumnWidths: number[];
  lastSeed: number;
  memoryUseWarningMb: number;
  memoryUseEverySec: number;
};

export type ActivityType = 'BACKUP' | 'RESTORE' | 'DELETE';

export type Activity = {
  dateTime: string;
  activityType: ActivityType;
  fileName: string;
};

export type ActivityList = {
  activities: Activity[];
};

export type BackupEntry = {
  name: string;
  path: string;
  size: number;
  lastModified: string;
};

export type ActionType =
  | 'ACTION_TYPE_PROJECTILE'
  | 'ACTION_TYPE_STATIC_PROJECTILE'
  | 'ACTION_TYPE_MATERIAL'
  | 'ACTION_TYPE_UTILITY'
  | 'ACTION_TYPE_DRAW_MANY'
  | 'ACTION_TYPE_MODIFIER'
  | 'ACTION_TYPE_OTHER'
  | 'ACTION_TYPE_PASSIVE';

export type Spell = {
  id: string;
  name: string;
  description: string;
  english_name: string;
  english_desc: string;
  type: ActionType;
  spawn_level: number[];
  spawn_probability: number[];
  price: number;
  mana: number;
  max_uses: number;
  never_unlimited: boolean;
  spawn_manual_unlock: boolean;
  recursive: boolean;
  ai_never_uses: boolean;
  is_dangerous_blast: boolean;
  action: string[];
  sprite: string;
  sprite_unidentified: string;
  related_projectiles: string;
  custom_xml_file: string;
  spawn_requires_flag: string;
  sound_loop_tag: string;
  related_extra_entities: string;
  uiImageFilename: string;
};

export type Perk = {
  id: string;
  ui_name: string;
  ui_description: string;
  ui_icon: string;
  perk_icon: string;
  english_name: string;
  english_desc: string;
};

export type Wand = {
  shuffle: boolean;
  spellsCast: number;
  castDelay: number;
  rechargeTime: number;
  manaMax: number;
  manaChargeSpeed: number;
  capacity: number;
  spread: number;
  alwaysCasts: Spell[];
  spells: Spell[];
  spriteFile: string;
};

export type BoneWand = {
  fileName: string;
  lastModified: string;
  wand: Wand;
};

export type NoitaData = {
  translations: Record<string, string>;
  spells: Spell[];
  perks: Perk[];
  boneWands: BoneWand[];
};

export type MemoryStatus = {
  message: string;
  processFound: boolean;
  memoryMb?: number;
  overWarning: boolean;
};

export type NoitaUtilApi = {
  config: {
    load: () => Promise<AppConfig>;
    save: (config: AppConfig) => Promise<AppConfig>;
  };
  activityLog: {
    list: () => Promise<ActivityList>;
  };
  saves: {
    listBackups: () => Promise<BackupEntry[]>;
    createBackup: (name?: string) => Promise<BackupEntry>;
    restoreBackup: (name: string, backupFirst?: boolean) => Promise<void>;
    deleteBackups: (names: string[]) => Promise<BackupEntry[]>;
  };
  salakieli: {
    decryptAll: () => Promise<Record<string, string>>;
  };
  noitaData: {
    load: () => Promise<NoitaData>;
  };
  noitaProcess: {
    runSeed: (seed: number) => Promise<void>;
    getMemoryStatus: () => Promise<MemoryStatus>;
    startMemoryMonitor: () => Promise<void>;
    stopMemoryMonitor: () => Promise<void>;
    onMemoryStatus: (callback: (status: MemoryStatus) => void) => () => void;
  };
};

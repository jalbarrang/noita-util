import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { join } from 'node:path';
import type { Activity, ActivityList, ActivityType } from '../../../shared/types.js';
import { getConfigDir } from '../config/config-service.js';

const emptyActivityList = (): ActivityList => ({ activities: [] });

function activityLogFile(): string {
  return join(getConfigDir(), 'activity-log.json');
}

export async function getActivityLog(): Promise<ActivityList> {
  try {
    const raw = await readFile(activityLogFile(), 'utf8');
    const parsed = JSON.parse(raw) as Partial<ActivityList>;
    return { activities: Array.isArray(parsed.activities) ? parsed.activities : [] };
  } catch {
    return emptyActivityList();
  }
}

export async function logAction(activityType: ActivityType, fileName: string): Promise<Activity> {
  const activity: Activity = {
    dateTime: new Date().toISOString(),
    activityType,
    fileName,
  };
  const activityList = await getActivityLog();
  activityList.activities.push(activity);

  await mkdir(getConfigDir(), { recursive: true });
  await writeFile(activityLogFile(), `${JSON.stringify(activityList, null, 2)}\n`, 'utf8');

  return activity;
}

export function cleanup(value: string): string {
  let next = value.split(' --')[0]?.trim() ?? '';
  if (next.startsWith('"')) next = next.slice(1);
  if (next.endsWith(',')) next = next.slice(0, -1);
  if (next.endsWith('"')) next = next.slice(0, -1);
  return next;
}

export function groupWithSep(lines: string[], separator: (line: string) => boolean): string[][] {
  const groups: string[][] = [];
  let current: string[] = [];

  for (const line of lines) {
    current.push(line);
    if (separator(line)) {
      groups.push(current);
      current = [];
    }
  }

  return groups;
}

export function filterLuaComments(lines: string[]): string[] {
  const result: string[] = [];
  let inComment = false;

  for (const line of lines) {
    const trimmed = line.trim();
    if (inComment) {
      if (trimmed.endsWith(']]--')) {
        inComment = false;
      }
      continue;
    }
    if (trimmed.startsWith('--[[')) {
      inComment = true;
      continue;
    }
    result.push(line);
  }

  return result.filter((line) => !line.trim().startsWith('--'));
}

export function getRightOfEqual(line: string): string {
  return unquote(line.split('=').at(-1)?.trim() ?? '');
}

export function unquote(value: string): string {
  if (value.startsWith('"') && value.endsWith('",')) {
    return value.slice(1, -2);
  }
  if (value.startsWith('"') && value.endsWith('"')) {
    return value.slice(1, -1);
  }
  return value;
}

export function toIntViaFloat(value: string | undefined): number {
  return Number.parseInt((value ?? '0').split('.')[0] ?? '0', 10);
}

export function secondsFromFrames(value: string | undefined): number {
  return Number((Number(value ?? 0) / 60).toFixed(2));
}

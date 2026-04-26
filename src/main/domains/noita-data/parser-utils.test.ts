import { describe, expect, it } from 'vitest';
import {
  cleanup,
  filterLuaComments,
  getRightOfEqual,
  groupWithSep,
  unquote,
} from './parser-utils.js';

describe('parser utils', () => {
  it('matches legacy cleanup behavior', () => {
    expect(cleanup('"$spell_name", -- comment')).toBe('$spell_name');
    expect(cleanup('"ACTION_TYPE_PROJECTILE",')).toBe('ACTION_TYPE_PROJECTILE');
  });

  it('groups lines including the separator', () => {
    expect(groupWithSep(['a', '},', 'b', '},'], (line) => line === '},')).toEqual([
      ['a', '},'],
      ['b', '},'],
    ]);
  });

  it('filters Lua line and block comments like the Kotlin helper', () => {
    expect(filterLuaComments(['keep', '-- line', '--[[', 'hidden', ']]--', 'after'])).toEqual([
      'keep',
      'after',
    ]);
  });

  it('extracts and unquotes right side values', () => {
    expect(getRightOfEqual('id = "FOO",')).toBe('FOO');
    expect(unquote('"BAR"')).toBe('BAR');
  });
});

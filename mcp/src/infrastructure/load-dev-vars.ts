import { readFileSync, existsSync } from "node:fs";
import { resolve } from "node:path";

/**
 * Load Cloudflare-style `.dev.vars` / `.env` into process.env for local stdio.
 * Does not override variables already set in the environment.
 * Safe no-op if the file is missing.
 */
export function loadDevVars(cwd: string = process.cwd()): string | null {
  const candidates = [".dev.vars", ".env"];
  let loadedFrom: string | null = null;

  for (const name of candidates) {
    const path = resolve(cwd, name);
    if (!existsSync(path)) continue;

    const text = readFileSync(path, "utf8");
    for (const line of text.split(/\r?\n/)) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith("#")) continue;

      const eq = trimmed.indexOf("=");
      if (eq <= 0) continue;

      const key = trimmed.slice(0, eq).trim();
      let value = trimmed.slice(eq + 1).trim();

      // Strip optional surrounding quotes
      if (
        (value.startsWith('"') && value.endsWith('"')) ||
        (value.startsWith("'") && value.endsWith("'"))
      ) {
        value = value.slice(1, -1);
      }

      if (key && process.env[key] === undefined) {
        process.env[key] = value;
      }
    }

    loadedFrom = path;
    break;
  }

  return loadedFrom;
}

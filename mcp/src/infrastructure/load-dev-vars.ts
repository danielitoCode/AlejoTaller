import { readFileSync, existsSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

/**
 * Load Cloudflare-style `.dev.vars` / `.env` into process.env for local stdio.
 * Does not override variables already set in the environment.
 *
 * Search order (first hit wins):
 * 1. process.cwd() — when you `cd mcp && npm run inspect`
 * 2. package root of this file (…/mcp) — when Inspector cwd is monorepo root
 * 3. cwd/mcp — when cwd is AlejoTaller repo root
 */
export function loadDevVars(): string | null {
  const here = dirname(fileURLToPath(import.meta.url));
  // …/mcp/src/infrastructure → …/mcp
  const packageRoot = resolve(here, "..", "..");

  const dirs = [
    process.cwd(),
    packageRoot,
    resolve(process.cwd(), "mcp"),
  ];

  const seen = new Set<string>();
  for (const dir of dirs) {
    const normalized = resolve(dir);
    if (seen.has(normalized)) continue;
    seen.add(normalized);

    const loaded = tryLoadInDir(normalized);
    if (loaded) return loaded;
  }

  return null;
}

function tryLoadInDir(dir: string): string | null {
  for (const name of [".dev.vars", ".env"] as const) {
    const path = resolve(dir, name);
    if (!existsSync(path)) continue;

    const text = readFileSync(path, "utf8");
    let applied = 0;

    for (const line of text.split(/\r?\n/)) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith("#")) continue;

      const eq = trimmed.indexOf("=");
      if (eq <= 0) continue;

      const key = trimmed.slice(0, eq).trim();
      let value = trimmed.slice(eq + 1).trim();

      if (
        (value.startsWith('"') && value.endsWith('"')) ||
        (value.startsWith("'") && value.endsWith("'"))
      ) {
        value = value.slice(1, -1);
      }

      if (key && process.env[key] === undefined) {
        process.env[key] = value;
        applied += 1;
      }
    }

    console.error(
      `[mcp] Loaded ${applied} env key(s) from ${path}`
    );
    return path;
  }
  return null;
}

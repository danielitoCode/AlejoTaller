#!/usr/bin/env node
/**
 * Cross-platform runner for live health smoke (Windows CMD/PowerShell + Unix).
 * Sets MCP_SMOKE_LIVE=1 and runs vitest on the health smoke file.
 */
import { spawn } from "node:child_process";
import { fileURLToPath } from "node:url";
import path from "node:path";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");

process.env.MCP_SMOKE_LIVE = "1";

const vitestBin = path.join(
  root,
  "node_modules",
  ".bin",
  process.platform === "win32" ? "vitest.cmd" : "vitest"
);

const child = spawn(
  vitestBin,
  ["run", "test/smoke/health.smoke.test.ts"],
  {
    cwd: root,
    env: process.env,
    stdio: "inherit",
    shell: process.platform === "win32",
  }
);

child.on("exit", (code, signal) => {
  if (signal) {
    process.exit(1);
  }
  process.exit(code ?? 1);
});

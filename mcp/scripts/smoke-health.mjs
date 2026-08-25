#!/usr/bin/env node
/**
 * Smoke — HTTP health del worker MCP (sin secretos Appwrite).
 *
 * Usage:
 *   node scripts/smoke-health.mjs
 *   MCP_SMOKE_URL=https://... node scripts/smoke-health.mjs
 *
 * Exit 0 = ok | Exit 1 = fail
 */

const DEFAULT_URL =
  "https://alejotaller-mcp.daniel-imbert96.workers.dev/health";

const base = (process.env.MCP_SMOKE_URL || DEFAULT_URL).replace(/\/$/, "");
const url = base.endsWith("/health") ? base : `${base}/health`;

const timeoutMs = Number(process.env.MCP_SMOKE_TIMEOUT_MS || 15_000);

async function main() {
  console.log(`[smoke-health] GET ${url}`);

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);

  let res;
  try {
    res = await fetch(url, {
      method: "GET",
      headers: { Accept: "application/json" },
      signal: controller.signal,
    });
  } catch (err) {
    clearTimeout(timer);
    const msg = err instanceof Error ? err.message : String(err);
    console.error(`[smoke-health] FAIL network: ${msg}`);
    process.exit(1);
  }
  clearTimeout(timer);

  const text = await res.text();
  let body;
  try {
    body = JSON.parse(text);
  } catch {
    console.error(`[smoke-health] FAIL non-JSON body (status ${res.status}):`, text.slice(0, 200));
    process.exit(1);
  }

  console.log("[smoke-health] status", res.status, body);

  if (!res.ok) {
    console.error(`[smoke-health] FAIL HTTP ${res.status}`);
    process.exit(1);
  }

  if (body.status !== "ok" && body.status !== "degraded") {
    console.error(`[smoke-health] FAIL unexpected status field: ${body.status}`);
    process.exit(1);
  }

  if (body.worker !== "alejotaller-mcp") {
    console.error(`[smoke-health] FAIL unexpected worker: ${body.worker}`);
    process.exit(1);
  }

  if (body.scope !== "b2c-customer") {
    console.error(`[smoke-health] FAIL unexpected scope: ${body.scope}`);
    process.exit(1);
  }

  if (body.status === "degraded") {
    console.warn(
      "[smoke-health] WARN worker is degraded (Appwrite secrets missing on edge)"
    );
  }

  console.log("[smoke-health] OK");
  process.exit(0);
}

main();

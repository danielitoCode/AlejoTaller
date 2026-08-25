#!/usr/bin/env node
/**
 * Smoke Fase 2 — integración MCP (health + tools/list + tools/call ping).
 * Sin navegador. No requiere JWT para tools públicas de sistema.
 *
 * Env:
 *   MCP_BASE_URL | VITE_MCP_BASE_URL
 *   (opcional) MCP_SMOKE_CUSTOMER_ID | JWT via MCP_SMOKE_JWT
 *
 *   cd web && npm run smoke:mcp_integration
 */

import { readFileSync, existsSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const DEFAULT_BASE =
  "https://alejotaller-mcp.daniel-imbert96.workers.dev";
const PROTOCOL_VERSION = "2025-03-26";

function loadEnvFile(filePath) {
  if (!existsSync(filePath)) return;
  const text = readFileSync(filePath, "utf8");
  for (const line of text.split(/\r?\n/)) {
    const t = line.trim();
    if (!t || t.startsWith("#")) continue;
    const eq = t.indexOf("=");
    if (eq <= 0) continue;
    const key = t.slice(0, eq).trim();
    let val = t.slice(eq + 1).trim();
    if (
      (val.startsWith('"') && val.endsWith('"')) ||
      (val.startsWith("'") && val.endsWith("'"))
    ) {
      val = val.slice(1, -1);
    }
    if (key && process.env[key] === undefined) process.env[key] = val;
  }
}

loadEnvFile(resolve(root, ".env"));
loadEnvFile(resolve(root, ".env.local"));

function env(...keys) {
  for (const k of keys) {
    const v = process.env[k]?.trim();
    if (v) return v;
  }
  return "";
}

const baseUrl = (
  env("MCP_BASE_URL", "VITE_MCP_BASE_URL") || DEFAULT_BASE
).replace(/\/$/, "");
const jwt = env("MCP_SMOKE_JWT");
const customerId = env("MCP_SMOKE_CUSTOMER_ID", "X_CUSTOMER_ID");
const timeoutMs = Number(process.env.MCP_SMOKE_TIMEOUT_MS || 25_000);

function fail(msg, code = 1) {
  console.error(`[smoke-mcp] FAIL: ${msg}`);
  process.exit(code);
}

function ok(msg) {
  console.log(`[smoke-mcp] OK: ${msg}`);
}

function parseBody(contentType, text) {
  const ct = (contentType || "").toLowerCase();
  if (ct.includes("text/event-stream")) {
    let last = "";
    for (const line of text.split(/\r?\n/)) {
      if (line.startsWith("data:")) last = line.slice(5).trim();
    }
    if (!last) throw new Error("SSE vacío");
    return JSON.parse(last);
  }
  return JSON.parse(text);
}

async function fetchTimeout(url, init) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    return await fetch(url, { ...init, signal: controller.signal });
  } finally {
    clearTimeout(timer);
  }
}

async function main() {
  console.log("[smoke-mcp] MCP integration smoke");
  console.log(`[smoke-mcp] base=${baseUrl}`);

  // 1) Health
  {
    const url = `${baseUrl}/health`;
    console.log(`[smoke-mcp] GET ${url}`);
    const res = await fetchTimeout(url, {
      method: "GET",
      headers: { Accept: "application/json" },
    });
    const text = await res.text();
    let body;
    try {
      body = JSON.parse(text);
    } catch {
      fail(`health non-JSON HTTP ${res.status}: ${text.slice(0, 120)}`);
    }
    if (!res.ok) fail(`health HTTP ${res.status}`);
    if (body.status !== "ok" && body.status !== "degraded") {
      fail(`health status=${body.status}`);
    }
    if (body.worker !== "alejotaller-mcp") {
      fail(`unexpected worker=${body.worker}`);
    }
    ok(`health status=${body.status} appwriteConfigured=${body.appwriteConfigured}`);
  }

  const extraHeaders = {};
  if (jwt) {
    extraHeaders["Authorization"] = jwt.startsWith("Bearer ")
      ? jwt
      : `Bearer ${jwt}`;
  }
  if (customerId) {
    extraHeaders["X-Customer-Id"] = customerId;
  }

  let sessionId = null;
  let rpcId = 1;

  async function rpc(payload, captureSession = false) {
    const headers = {
      "Content-Type": "application/json",
      Accept: "application/json, text/event-stream",
      ...extraHeaders,
    };
    if (sessionId) headers["Mcp-Session-Id"] = sessionId;

    const res = await fetchTimeout(baseUrl, {
      method: "POST",
      headers,
      body: JSON.stringify(payload),
    });
    if (captureSession) {
      sessionId =
        res.headers.get("Mcp-Session-Id") ||
        res.headers.get("mcp-session-id") ||
        sessionId;
    }
    const text = await res.text();
    if (!text.trim()) {
      return { res, body: null };
    }
    const body = parseBody(res.headers.get("content-type") || "", text);
    return { res, body };
  }

  // 2) initialize
  console.log("[smoke-mcp] initialize…");
  {
    const { res, body } = await rpc(
      {
        jsonrpc: "2.0",
        id: rpcId++,
        method: "initialize",
        params: {
          protocolVersion: PROTOCOL_VERSION,
          capabilities: {},
          clientInfo: { name: "smoke-mcp-integration", version: "0.1.0" },
        },
      },
      true
    );
    if (!res.ok) {
      fail(`initialize HTTP ${res.status} ${JSON.stringify(body)?.slice?.(0, 200) || ""}`);
    }
    if (body?.error) fail(`initialize: ${body.error.message}`);
    ok(`initialize session=${sessionId ? sessionId.slice(0, 12) + "…" : "(none)"}`);
  }

  // notification
  await rpc({ jsonrpc: "2.0", method: "notifications/initialized" });

  // 3) tools/list
  console.log("[smoke-mcp] tools/list…");
  let toolNames = [];
  {
    const { res, body } = await rpc({
      jsonrpc: "2.0",
      id: rpcId++,
      method: "tools/list",
      params: {},
    });
    if (!res.ok) fail(`tools/list HTTP ${res.status}`);
    if (body?.error) fail(`tools/list: ${body.error.message}`);
    const tools = body?.result?.tools;
    if (!Array.isArray(tools) || tools.length === 0) {
      fail("tools/list vacío");
    }
    toolNames = tools.map((t) => t.name).filter(Boolean);
    ok(`tools/list count=${toolNames.length} sample=${toolNames.slice(0, 5).join(",")}`);
  }

  // 4) tools/call ping_customer_mcp
  const pingName = toolNames.includes("ping_customer_mcp")
    ? "ping_customer_mcp"
    : toolNames[0];
  console.log(`[smoke-mcp] tools/call ${pingName}…`);
  {
    const { res, body } = await rpc({
      jsonrpc: "2.0",
      id: rpcId++,
      method: "tools/call",
      params: { name: pingName, arguments: {} },
    });
    if (!res.ok) fail(`tools/call HTTP ${res.status}`);
    if (body?.error) fail(`tools/call: ${body.error.message}`);
    const isError = body?.result?.isError === true;
    if (isError) fail(`tools/call isError=true`);
    const content = body?.result?.content;
    let text = "";
    if (Array.isArray(content)) {
      text = content
        .map((c) => (c && c.text != null ? String(c.text) : ""))
        .join(" ")
        .trim();
    }
    ok(`tools/call ${pingName} → ${text.slice(0, 100) || "(ok)"}`);
  }

  console.log("[smoke-mcp] Fase 2 smoke PASSED");
  process.exit(0);
}

main().catch((e) => fail(e instanceof Error ? e.message : String(e)));

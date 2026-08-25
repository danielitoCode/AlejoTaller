#!/usr/bin/env node
/**
 * Smoke Fase 1 — Mistral API por HTTP (sin SDK npm).
 *
 * Env:
 *   MISTRAL_API_KEY | VITE_MISTRAL_API_KEY
 *   MISTRAL_AGENT_ID | VITE_MISTRAL_AGENT_ID
 *   MISTRAL_MODEL_ID | VITE_MISTRAL_MODEL_ID
 *
 *   cd web && npm run smoke:agent
 */

import { readFileSync, existsSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const BASE = "https://api.mistral.ai/v1";

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

const apiKey = env("MISTRAL_API_KEY", "VITE_MISTRAL_API_KEY");
const agentId = env("MISTRAL_AGENT_ID", "VITE_MISTRAL_AGENT_ID");
const modelId =
  env("MISTRAL_MODEL_ID", "VITE_MISTRAL_MODEL_ID") || "mistral-medium-latest";
const strict = process.env.AGENT_SMOKE_STRICT === "1";

function fail(msg, code = 1) {
  console.error(`[smoke-agent] FAIL: ${msg}`);
  process.exit(code);
}

function ok(msg) {
  console.log(`[smoke-agent] OK: ${msg}`);
}

async function main() {
  console.log("[smoke-agent] Mistral HTTP smoke (sin SDK npm)");
  console.log(
    `[smoke-agent] modelId=${modelId} agentId=${agentId ? agentId.slice(0, 12) + "…" : "(empty)"}`
  );

  if (!apiKey) {
    fail("Falta MISTRAL_API_KEY o VITE_MISTRAL_API_KEY", strict ? 1 : 2);
  }
  if (!agentId) {
    fail("Falta MISTRAL_AGENT_ID o VITE_MISTRAL_AGENT_ID", strict ? 1 : 2);
  }

  const headers = {
    Authorization: `Bearer ${apiKey}`,
    Accept: "application/json",
    "Content-Type": "application/json",
  };

  console.log(`[smoke-agent] GET /models/${modelId}…`);
  {
    const res = await fetch(`${BASE}/models/${encodeURIComponent(modelId)}`, {
      method: "GET",
      headers,
    });
    const body = await res.text();
    if (!res.ok) fail(`models: HTTP ${res.status} ${body.slice(0, 200)}`);
    let id = modelId;
    try {
      id = JSON.parse(body)?.id ?? modelId;
    } catch {
      /* ignore */
    }
    ok(`models → ${id}`);
  }

  console.log("[smoke-agent] POST /agents/completions…");
  {
    const res = await fetch(`${BASE}/agents/completions`, {
      method: "POST",
      headers,
      body: JSON.stringify({
        agent_id: agentId,
        messages: [
          {
            role: "user",
            content:
              "Responde solo con la palabra PONG (una palabra, sin explicación).",
          },
        ],
      }),
    });
    const body = await res.text();
    if (!res.ok) fail(`agents: HTTP ${res.status} ${body.slice(0, 300)}`);
    let text = "";
    try {
      const data = JSON.parse(body);
      const raw = data?.choices?.[0]?.message?.content;
      text = typeof raw === "string" ? raw : String(raw ?? "");
    } catch {
      text = body;
    }
    text = text.trim();
    if (!text) fail("agents: respuesta vacía");
    ok(`agents → "${text.slice(0, 120)}${text.length > 120 ? "…" : ""}"`);
  }

  console.log("[smoke-agent] Fase 1 smoke PASSED");
  process.exit(0);
}

main().catch((e) => fail(e instanceof Error ? e.message : String(e)));

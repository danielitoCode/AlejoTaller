#!/usr/bin/env node
/**
 * Smoke Fase 1 — Mistral official SDK (`@mistralai/mistralai`).
 *
 * Env (cualquiera de estos nombres):
 *   MISTRAL_API_KEY | VITE_MISTRAL_API_KEY
 *   MISTRAL_AGENT_ID | VITE_MISTRAL_AGENT_ID
 *   MISTRAL_MODEL_ID | VITE_MISTRAL_MODEL_ID  (default mistral-medium-latest)
 *
 * Carga opcional de web/.env y web/.env.local (sin sobrescribir process.env).
 *
 * Usage:
 *   cd web && npm run smoke:agent
 *
 * Exit 0 = ok | 1 = fail | 2 = no configurado (salvo AGENT_SMOKE_STRICT=1)
 */

import { readFileSync, existsSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { Mistral } from "@mistralai/mistralai";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");

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
    if (key && process.env[key] === undefined) {
      process.env[key] = val;
    }
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
  console.log("[smoke-agent] Mistral SDK smoke (Fase 1)");
  console.log(`[smoke-agent] modelId=${modelId} agentId=${agentId ? agentId.slice(0, 12) + "…" : "(empty)"}`);

  if (!apiKey) {
    fail(
      "Falta MISTRAL_API_KEY o VITE_MISTRAL_API_KEY (env o web/.env)",
      strict ? 1 : 2
    );
  }
  if (!agentId) {
    fail(
      "Falta MISTRAL_AGENT_ID o VITE_MISTRAL_AGENT_ID (env o web/.env)",
      strict ? 1 : 2
    );
  }

  const client = new Mistral({ apiKey });

  // 1) Connection probe — models.retrieve (GET /v1/models/{model_id})
  console.log(`[smoke-agent] models.retrieve(${modelId})…`);
  try {
    const model = await client.models.retrieve({ modelId });
    const id = model?.id ?? modelId;
    ok(`models.retrieve → ${id}`);
  } catch (err) {
    const msg = err instanceof Error ? err.message : String(err);
    fail(`models.retrieve: ${msg.slice(0, 300)}`);
  }

  // 2) Agent completion — agents.complete
  console.log("[smoke-agent] agents.complete (ping corto)…");
  try {
    const result = await client.agents.complete({
      agentId,
      messages: [
        {
          role: "user",
          content:
            "Responde solo con la palabra PONG (una palabra, sin explicación).",
        },
      ],
      responseFormat: { type: "text" },
    });

    const raw = result?.choices?.[0]?.message?.content;
    let text = "";
    if (typeof raw === "string") text = raw;
    else if (Array.isArray(raw)) {
      text = raw
        .map((p) =>
          typeof p === "string"
            ? p
            : p && typeof p === "object" && "text" in p
              ? String(p.text)
              : ""
        )
        .join("");
    } else if (raw != null) text = String(raw);

    text = text.trim();
    if (!text) fail("agents.complete devolvió contenido vacío");

    ok(`agents.complete → "${text.slice(0, 120)}${text.length > 120 ? "…" : ""}"`);
    console.log(`[smoke-agent] providerId=${result?.id ?? "n/a"}`);
  } catch (err) {
    const msg = err instanceof Error ? err.message : String(err);
    fail(`agents.complete: ${msg.slice(0, 400)}`);
  }

  console.log("[smoke-agent] Fase 1 smoke PASSED");
  process.exit(0);
}

main().catch((err) => {
  fail(err instanceof Error ? err.message : String(err));
});

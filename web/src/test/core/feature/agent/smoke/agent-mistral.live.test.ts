import { describe, it, expect } from "vitest";
import { Mistral } from "@mistralai/mistralai";

/**
 * Live smoke — solo con AGENT_SMOKE_LIVE=1 y keys en env.
 * No corre en CI por defecto.
 */
const LIVE = process.env.AGENT_SMOKE_LIVE === "1";

function pick(...keys: string[]): string {
  for (const k of keys) {
    const v = process.env[k]?.trim();
    if (v) return v;
  }
  return "";
}

describe.runIf(LIVE)("Mistral SDK live smoke (Fase 1)", () => {
  const apiKey = pick("MISTRAL_API_KEY", "VITE_MISTRAL_API_KEY");
  const agentId = pick("MISTRAL_AGENT_ID", "VITE_MISTRAL_AGENT_ID");
  const modelId =
    pick("MISTRAL_MODEL_ID", "VITE_MISTRAL_MODEL_ID") ||
    "mistral-medium-latest";

  it("models.retrieve + agents.complete", async () => {
    expect(apiKey, "MISTRAL_API_KEY / VITE_MISTRAL_API_KEY").toBeTruthy();
    expect(agentId, "MISTRAL_AGENT_ID / VITE_MISTRAL_AGENT_ID").toBeTruthy();

    const client = new Mistral({ apiKey });

    const model = await client.models.retrieve({ modelId });
    expect(model?.id || modelId).toBeTruthy();

    const result = await client.agents.complete({
      agentId,
      messages: [
        {
          role: "user",
          content: "Responde solo con la palabra PONG.",
        },
      ],
      responseFormat: { type: "text" },
    });

    const raw = result.choices?.[0]?.message?.content;
    const text =
      typeof raw === "string"
        ? raw
        : Array.isArray(raw)
          ? raw.map((p) => (typeof p === "string" ? p : "")).join("")
          : String(raw ?? "");

    expect(text.trim().length).toBeGreaterThan(0);
  }, 60_000);
});

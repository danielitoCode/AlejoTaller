import { describe, it, expect } from "vitest";

/** Live smoke HTTP — AGENT_SMOKE_LIVE=1 + keys en env. Sin SDK npm. */
const LIVE = process.env.AGENT_SMOKE_LIVE === "1";
const BASE = "https://api.mistral.ai/v1";

function pick(...keys: string[]): string {
  for (const k of keys) {
    const v = process.env[k]?.trim();
    if (v) return v;
  }
  return "";
}

describe.runIf(LIVE)("Mistral HTTP live smoke (Fase 1)", () => {
  const apiKey = pick("MISTRAL_API_KEY", "VITE_MISTRAL_API_KEY");
  const agentId = pick("MISTRAL_AGENT_ID", "VITE_MISTRAL_AGENT_ID");
  const modelId =
    pick("MISTRAL_MODEL_ID", "VITE_MISTRAL_MODEL_ID") ||
    "mistral-medium-latest";

  it("GET models + POST agents/completions", async () => {
    expect(apiKey).toBeTruthy();
    expect(agentId).toBeTruthy();

    const headers = {
      Authorization: `Bearer ${apiKey}`,
      Accept: "application/json",
      "Content-Type": "application/json",
    };

    const modelRes = await fetch(
      `${BASE}/models/${encodeURIComponent(modelId)}`,
      { headers }
    );
    expect(modelRes.ok).toBe(true);

    const agentRes = await fetch(`${BASE}/agents/completions`, {
      method: "POST",
      headers,
      body: JSON.stringify({
        agent_id: agentId,
        messages: [{ role: "user", content: "Responde solo con la palabra PONG." }],
      }),
    });
    expect(agentRes.ok).toBe(true);
    const data = (await agentRes.json()) as {
      choices?: Array<{ message?: { content?: string } }>;
    };
    const text = data.choices?.[0]?.message?.content?.trim() ?? "";
    expect(text.length).toBeGreaterThan(0);
  }, 60_000);
});

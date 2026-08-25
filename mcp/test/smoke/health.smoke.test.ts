import { describe, it, expect } from "vitest";

/**
 * Optional live health probe.
 * Skipped unless MCP_SMOKE_LIVE=1 (CI smoke job / manual).
 * Does not require Appwrite credentials — only public /health.
 */
const LIVE = process.env.MCP_SMOKE_LIVE === "1";
const BASE =
  process.env.MCP_SMOKE_URL?.replace(/\/$/, "") ??
  "https://alejotaller-mcp.daniel-imbert96.workers.dev";

describe.runIf(LIVE)("live health smoke", () => {
  it("GET /health returns worker payload", async () => {
    const url = BASE.endsWith("/health") ? BASE : `${BASE}/health`;
    const res = await fetch(url, { headers: { Accept: "application/json" } });
    expect(res.ok).toBe(true);
    const body = (await res.json()) as {
      status: string;
      worker: string;
      scope: string;
      transport?: string;
    };
    expect(["ok", "degraded"]).toContain(body.status);
    expect(body.worker).toBe("alejotaller-mcp");
    expect(body.scope).toBe("b2c-customer");
  }, 20_000);
});

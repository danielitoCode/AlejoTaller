import { describe, expect, it } from "vitest";
import { parseMcpHttpBody } from "../../../../core/feature/agent/data/repository/mcp.http.repository";

describe("parseMcpHttpBody", () => {
  it("parsea application/json", () => {
    const body = parseMcpHttpBody(
      "application/json",
      JSON.stringify({ jsonrpc: "2.0", id: 1, result: { tools: [] } })
    );
    expect(body.result).toEqual({ tools: [] });
  });

  it("parsea SSE data:", () => {
    const sse =
      "event: message\ndata: {\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"ok\":true}}\n\n";
    const body = parseMcpHttpBody("text/event-stream", sse);
    expect(body.result).toEqual({ ok: true });
  });
});

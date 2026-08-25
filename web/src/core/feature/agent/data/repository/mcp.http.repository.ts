import type {
  McpAuthContext,
  McpHealthResult,
  McpToolCallResult,
  McpToolContentPart,
  McpToolDescriptor,
} from "../../domain/entity/McpTypes";
import type { McpGateway } from "../../domain/repository/mcp.gateway";

export interface McpHttpConfig {
  /** e.g. https://alejotaller-mcp.daniel-imbert96.workers.dev */
  baseUrl: string;
  timeoutMs?: number;
}

const DEFAULT_TIMEOUT_MS = 25_000;
const PROTOCOL_VERSION = "2025-03-26";

type JsonRpcId = number | string;

interface JsonRpcSuccess {
  jsonrpc: "2.0";
  id: JsonRpcId;
  result?: unknown;
  error?: { code: number; message: string; data?: unknown };
}

function stripTrailingSlash(url: string): string {
  return url.replace(/\/$/, "");
}

function authHeaders(auth?: McpAuthContext | null): Record<string, string> {
  const h: Record<string, string> = {};
  const jwt = auth?.jwt?.trim();
  if (jwt) {
    h["Authorization"] = jwt.startsWith("Bearer ") ? jwt : `Bearer ${jwt}`;
  }
  const cid = auth?.customerId?.trim();
  if (cid) h["X-Customer-Id"] = cid;
  const name = auth?.customerName?.trim();
  if (name) h["X-Customer-Name"] = name;
  const email = auth?.customerEmail?.trim();
  if (email) h["X-Customer-Email"] = email;
  return h;
}

/** Parse application/json or text/event-stream body into a JSON-RPC object. */
export function parseMcpHttpBody(contentType: string, text: string): JsonRpcSuccess {
  const ct = contentType.toLowerCase();
  if (ct.includes("text/event-stream")) {
    const lines = text.split(/\r?\n/);
    let lastData = "";
    for (const line of lines) {
      if (line.startsWith("data:")) {
        lastData = line.slice(5).trim();
      }
    }
    if (!lastData) {
      throw new Error("MCP SSE vacío (sin data:)");
    }
    return JSON.parse(lastData) as JsonRpcSuccess;
  }
  return JSON.parse(text) as JsonRpcSuccess;
}

function flattenToolContent(content: unknown): {
  text: string;
  parts: McpToolContentPart[];
} {
  if (!Array.isArray(content)) {
    const t = content == null ? "" : String(content);
    return { text: t, parts: t ? [{ type: "text", text: t }] : [] };
  }
  const parts: McpToolContentPart[] = [];
  const texts: string[] = [];
  for (const item of content) {
    if (!item || typeof item !== "object") continue;
    const o = item as Record<string, unknown>;
    const type = String(o.type ?? "text");
    const text = o.text != null ? String(o.text) : undefined;
    parts.push({ type, text });
    if (text) texts.push(text);
  }
  return { text: texts.join("\n").trim(), parts };
}

/**
 * Cliente MCP Streamable HTTP (fetch nativo, sin SDK npm).
 */
export class McpHttpRepository implements McpGateway {
  private readonly baseUrl: string;
  private readonly timeoutMs: number;
  private sessionId: string | null = null;
  private rpcId = 1;

  constructor(config: McpHttpConfig) {
    this.baseUrl = stripTrailingSlash(config.baseUrl.trim());
    this.timeoutMs = config.timeoutMs ?? DEFAULT_TIMEOUT_MS;
  }

  async checkHealth(): Promise<McpHealthResult> {
    if (!this.baseUrl) {
      return {
        status: "error",
        worker: null,
        scope: null,
        appwriteConfigured: null,
        message: "Falta VITE_MCP_BASE_URL / MCP_BASE_URL",
      };
    }
    const url = `${this.baseUrl}/health`;
    try {
      const res = await this.fetchWithTimeout(url, {
        method: "GET",
        headers: { Accept: "application/json" },
      });
      const text = await res.text();
      let body: Record<string, unknown> = {};
      try {
        body = JSON.parse(text) as Record<string, unknown>;
      } catch {
        return {
          status: "error",
          worker: null,
          scope: null,
          appwriteConfigured: null,
          message: `Health non-JSON HTTP ${res.status}`,
        };
      }
      const st = String(body.status ?? "");
      if (!res.ok) {
        return {
          status: "error",
          worker: body.worker != null ? String(body.worker) : null,
          scope: body.scope != null ? String(body.scope) : null,
          appwriteConfigured:
            typeof body.appwriteConfigured === "boolean"
              ? body.appwriteConfigured
              : null,
          message: `Health HTTP ${res.status}`,
          raw: body,
        };
      }
      const status: McpHealthResult["status"] =
        st === "ok" || st === "degraded" ? st : "error";
      return {
        status,
        worker: body.worker != null ? String(body.worker) : null,
        scope: body.scope != null ? String(body.scope) : null,
        appwriteConfigured:
          typeof body.appwriteConfigured === "boolean"
            ? body.appwriteConfigured
            : null,
        message:
          status === "ok"
            ? "MCP health ok"
            : status === "degraded"
              ? "MCP health degraded (secrets incompletos)"
              : `MCP health status inesperado: ${st}`,
        raw: body,
      };
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      return {
        status: "unreachable",
        worker: null,
        scope: null,
        appwriteConfigured: null,
        message: `MCP unreachable: ${msg.slice(0, 200)}`,
      };
    }
  }

  async listTools(
    auth?: McpAuthContext | null
  ): Promise<ReadonlyArray<McpToolDescriptor>> {
    await this.ensureSession(auth);
    const result = await this.rpc<{ tools?: unknown[] }>("tools/list", {}, auth);
    const tools = Array.isArray(result.tools) ? result.tools : [];
    return tools.map((t) => {
      const o = (t ?? {}) as Record<string, unknown>;
      const schema =
        o.inputSchema && typeof o.inputSchema === "object"
          ? (o.inputSchema as Record<string, unknown>)
          : {};
      return {
        name: String(o.name ?? ""),
        description: String(o.description ?? ""),
        inputSchema: schema,
      };
    });
  }

  async callTool(
    name: string,
    args: Record<string, unknown>,
    auth?: McpAuthContext | null
  ): Promise<McpToolCallResult> {
    await this.ensureSession(auth);
    const result = await this.rpc<{ content?: unknown; isError?: boolean }>(
      "tools/call",
      { name, arguments: args },
      auth
    );
    const { text, parts } = flattenToolContent(result.content);
    return {
      toolName: name,
      isError: result.isError === true,
      text,
      content: parts,
    };
  }

  private async ensureSession(auth?: McpAuthContext | null): Promise<void> {
    if (this.sessionId) return;
    const init = await this.rpcRaw(
      {
        jsonrpc: "2.0",
        id: this.nextId(),
        method: "initialize",
        params: {
          protocolVersion: PROTOCOL_VERSION,
          capabilities: {},
          clientInfo: { name: "alejotaller-web", version: "0.1.0" },
        },
      },
      auth,
      /* captureSession */ true
    );
    if (init.error) {
      throw new Error(
        `MCP initialize: ${init.error.message} (${init.error.code})`
      );
    }
    // notifications/initialized (no id)
    await this.rpcRaw(
      {
        jsonrpc: "2.0",
        method: "notifications/initialized",
      },
      auth,
      false
    );
  }

  private async rpc<T>(
    method: string,
    params: Record<string, unknown>,
    auth?: McpAuthContext | null
  ): Promise<T> {
    const id = this.nextId();
    const body = await this.rpcRaw(
      {
        jsonrpc: "2.0",
        id,
        method,
        params,
      },
      auth,
      false
    );
    if (body.error) {
      throw new Error(`MCP ${method}: ${body.error.message} (${body.error.code})`);
    }
    return body.result as T;
  }

  private async rpcRaw(
    payload: Record<string, unknown>,
    auth: McpAuthContext | null | undefined,
    captureSession: boolean
  ): Promise<JsonRpcSuccess> {
    if (!this.baseUrl) {
      throw new Error("MCP base URL no configurada");
    }
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
      Accept: "application/json, text/event-stream",
      ...authHeaders(auth),
    };
    if (this.sessionId) {
      headers["Mcp-Session-Id"] = this.sessionId;
    }

    const res = await this.fetchWithTimeout(this.baseUrl, {
      method: "POST",
      headers,
      body: JSON.stringify(payload),
    });

    if (captureSession) {
      const sid =
        res.headers.get("Mcp-Session-Id") ??
        res.headers.get("mcp-session-id");
      if (sid) this.sessionId = sid;
    }

    const text = await res.text();
    if (!res.ok && !text) {
      throw new Error(`MCP HTTP ${res.status} (empty body)`);
    }

    // Notification may return 202/204 with empty body
    if (!text.trim()) {
      return { jsonrpc: "2.0", id: 0, result: {} };
    }

    const contentType = res.headers.get("content-type") ?? "application/json";
    let parsed: JsonRpcSuccess;
    try {
      parsed = parseMcpHttpBody(contentType, text);
    } catch (e: unknown) {
      const msg = e instanceof Error ? e.message : String(e);
      throw new Error(`MCP parse: ${msg} · body=${text.slice(0, 160)}`);
    }

    if (!res.ok && parsed.error) {
      throw new Error(
        `MCP HTTP ${res.status}: ${parsed.error.message}`
      );
    }
    if (!res.ok) {
      throw new Error(`MCP HTTP ${res.status}: ${text.slice(0, 200)}`);
    }
    return parsed;
  }

  private nextId(): number {
    return this.rpcId++;
  }

  private async fetchWithTimeout(
    url: string,
    init: RequestInit
  ): Promise<Response> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), this.timeoutMs);
    try {
      return await fetch(url, { ...init, signal: controller.signal });
    } finally {
      clearTimeout(timer);
    }
  }
}

/**
 * CORS allowlist for the MCP worker.
 * Never reflects arbitrary Origin when credentials may be used with JWT.
 */

export interface CorsConfig {
  /** Comma-separated origins from env, or ["*"] for open (dev only) */
  allowedOrigins: string[];
}

export function parseCorsOrigins(raw: string | undefined): string[] {
  if (!raw || raw.trim() === "") {
    return ["*"];
  }
  return raw
    .split(",")
    .map((s) => s.trim())
    .filter((s) => s.length > 0);
}

/**
 * Resolve Access-Control-Allow-Origin for this request.
 * Returns null when the origin is not allowed (caller should omit ACAO or 403).
 */
export function resolveAllowOrigin(
  requestOrigin: string | null | undefined,
  config: CorsConfig
): string | null {
  const list = config.allowedOrigins;
  if (list.includes("*")) {
    return "*";
  }
  if (!requestOrigin) {
    return null;
  }
  if (list.includes(requestOrigin)) {
    return requestOrigin;
  }
  return null;
}

export function buildCorsHeaders(
  allowOrigin: string | null,
  extra?: Record<string, string>
): Record<string, string> {
  const headers: Record<string, string> = {
    "Access-Control-Allow-Methods": "GET, POST, DELETE, OPTIONS",
    "Access-Control-Allow-Headers":
      "Content-Type, Accept, Mcp-Session-Id, Last-Event-ID, X-Customer-Id, X-Customer-Name, X-Customer-Email, Authorization",
    "Access-Control-Expose-Headers": "Mcp-Session-Id",
    "Access-Control-Max-Age": "86400",
    ...extra,
  };
  if (allowOrigin) {
    headers["Access-Control-Allow-Origin"] = allowOrigin;
    if (allowOrigin !== "*") {
      headers["Vary"] = "Origin";
    }
  }
  return headers;
}

export function isOriginAllowed(
  requestOrigin: string | null | undefined,
  config: CorsConfig
): boolean {
  if (config.allowedOrigins.includes("*")) return true;
  // Non-browser clients (no Origin) are allowed — MCP hosts often omit Origin
  if (!requestOrigin) return true;
  return config.allowedOrigins.includes(requestOrigin);
}

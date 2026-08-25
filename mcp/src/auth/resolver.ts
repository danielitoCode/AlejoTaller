import type { AppwriteConfig } from "../infrastructure/appwrite/config.js";
import { type McpAuthContext, AuthenticationError } from "./context.js";
import { extractBearerToken, verifyAppwriteJwt } from "./jwt.js";

/**
 * Auth modes:
 * - header: only X-Customer-Id (legacy / trusted MCP host)
 * - jwt: only Authorization Bearer Appwrite JWT
 * - jwt_or_header: prefer JWT, fallback header (default during migration)
 */
export type AuthMode = "header" | "jwt" | "jwt_or_header";

export function parseAuthMode(raw: string | undefined): AuthMode {
  const v = (raw ?? "jwt_or_header").trim().toLowerCase();
  if (v === "header" || v === "jwt" || v === "jwt_or_header") return v;
  return "jwt_or_header";
}

export interface ResolveAuthOptions {
  mode: AuthMode;
  /** Required when mode is jwt or jwt_or_header and a Bearer token is present */
  appwriteConfig?: AppwriteConfig;
}

function getHeader(
  headers: Record<string, string | string[] | undefined>,
  key: string
): string | null {
  const value = headers[key] ?? headers[key.toLowerCase()];
  if (Array.isArray(value)) return value[0] ?? null;
  return value ?? null;
}

function resolveFromCustomerHeaders(
  headers: Record<string, string | string[] | undefined>
): McpAuthContext {
  const userId = getHeader(headers, "x-customer-id");
  const userName = getHeader(headers, "x-customer-name") ?? "Customer";
  const userEmail = getHeader(headers, "x-customer-email") ?? "";

  if (!userId || userId.trim() === "") {
    throw new AuthenticationError(
      "Missing X-Customer-Id header. The MCP host must provide a valid customer identity."
    );
  }

  return {
    userId: userId.trim(),
    userName: userName.trim(),
    userEmail: userEmail.trim(),
  };
}

/**
 * Resolve auth from HTTP headers (Streamable HTTP worker).
 * Prefer Bearer JWT when present and mode allows it.
 */
export async function resolveAuthContext(
  headers: Record<string, string | string[] | undefined>,
  options: ResolveAuthOptions
): Promise<McpAuthContext> {
  const authorization =
    getHeader(headers, "authorization") ?? getHeader(headers, "Authorization");
  const bearer = extractBearerToken(authorization);

  if (options.mode === "jwt") {
    if (!bearer) {
      throw new AuthenticationError(
        "Se requiere Authorization: Bearer <appwrite-jwt>"
      );
    }
    if (!options.appwriteConfig) {
      throw new AuthenticationError("Appwrite config missing for JWT verification");
    }
    return verifyAppwriteJwt(bearer, options.appwriteConfig);
  }

  if (options.mode === "header") {
    return resolveFromCustomerHeaders(headers);
  }

  // jwt_or_header
  if (bearer) {
    if (!options.appwriteConfig) {
      throw new AuthenticationError("Appwrite config missing for JWT verification");
    }
    return verifyAppwriteJwt(bearer, options.appwriteConfig);
  }

  return resolveFromCustomerHeaders(headers);
}

/**
 * Resolve auth from tool-call meta (stdio transport).
 * Supports customerId header-style fields or jwt string in meta.jwt.
 */
export async function resolveAuthContextFromMeta(
  meta: Record<string, unknown> | undefined,
  options?: ResolveAuthOptions
): Promise<McpAuthContext> {
  const mode = options?.mode ?? "jwt_or_header";

  const metaJwt = typeof meta?.["jwt"] === "string" ? meta["jwt"] : null;
  if ((mode === "jwt" || mode === "jwt_or_header") && metaJwt) {
    if (!options?.appwriteConfig) {
      throw new AuthenticationError("Appwrite config missing for JWT verification");
    }
    return verifyAppwriteJwt(metaJwt, options.appwriteConfig);
  }

  if (mode === "jwt") {
    throw new AuthenticationError(
      "Missing jwt in tool call meta. El cliente MCP debe enviar meta.jwt."
    );
  }

  const userId =
    typeof meta?.["customerId"] === "string" ? meta["customerId"] : null;
  const userName =
    typeof meta?.["customerName"] === "string" ? meta["customerName"] : "Customer";
  const userEmail =
    typeof meta?.["customerEmail"] === "string" ? meta["customerEmail"] : "";

  if (!userId || userId.trim() === "") {
    throw new AuthenticationError(
      "Missing customerId in tool call meta. The MCP client must provide customer identity."
    );
  }

  return {
    userId: userId.trim(),
    userName: userName.trim(),
    userEmail: userEmail.trim(),
  };
}

import { type McpAuthContext, AuthenticationError } from "./context.js";

/**
 * Auth — resolveAuthContext
 *
 * Extracts and validates the auth context from an incoming MCP request.
 *
 * PHASE 1 — Simple header-based identity:
 *   The MCP host passes X-Customer-Id, X-Customer-Name, X-Customer-Email
 *   headers. These are set by the MCP client configuration, NOT by the
 *   end user. The AI agent cannot forge these values.
 *
 * TODO (Production):
 *   Replace with Appwrite JWT verification:
 *   1. MCP host includes `Authorization: Bearer <appwrite-jwt>` header
 *   2. This function calls Appwrite Users.get() or validates the JWT
 *   3. Extracts userId, name, email from verified token
 *   4. Returns McpAuthContext
 *
 * @param headers - HTTP headers from the incoming request
 */
export function resolveAuthContext(
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
 * Resolve auth from a simple key-value meta object.
 * Used in stdio transport where headers are not available.
 */
export function resolveAuthContextFromMeta(
  meta: Record<string, unknown> | undefined
): McpAuthContext {
  const userId = typeof meta?.["customerId"] === "string" ? meta["customerId"] : null;
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

function getHeader(
  headers: Record<string, string | string[] | undefined>,
  key: string
): string | null {
  const value = headers[key] ?? headers[key.toLowerCase()];
  if (Array.isArray(value)) return value[0] ?? null;
  return value ?? null;
}

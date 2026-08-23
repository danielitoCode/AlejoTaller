/**
 * Auth — McpAuthContext
 *
 * The authenticated identity of the customer making a tool call.
 * This context is resolved once per request and passed to all services.
 *
 * IMPORTANT: The identity comes from the server, not from user input.
 * A tool like `get_my_orders()` does NOT accept a customerId parameter.
 * The userId is always taken from this context.
 *
 * TODO (Production): Replace simple userId header with Appwrite JWT verification.
 * The MCP host (Claude, GPT, etc.) should pass a session token obtained
 * from the AlejoTaller login flow. The MCP server verifies the token with
 * the Appwrite API and extracts userId, name, and email from the claim.
 */
export interface McpAuthContext {
  /** Appwrite user document ID ($id from account.get()) */
  userId: string;
  /** User's display name */
  userName: string;
  /** User's email address */
  userEmail: string;
}

/** Sentinel for unauthenticated requests */
export class AuthenticationError extends Error {
  constructor(message = "Authentication required") {
    super(message);
    this.name = "AuthenticationError";
  }
}

/** Sentinel for authorization failures (authenticated but not allowed) */
export class AuthorizationError extends Error {
  constructor(message = "Not authorized to perform this action") {
    super(message);
    this.name = "AuthorizationError";
  }
}

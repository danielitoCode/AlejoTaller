import type { McpAuthContext } from "../../auth/context.js";
import { AuthenticationError } from "../../auth/context.js";
import { getToolPolicy } from "../../policies/tool-policy.js";
import { mapToolError, type ToolResult } from "./result.js";

/** Auth may be sync or async (JWT verification hits Appwrite). */
export type AuthResolver = (
  extra: unknown
) => McpAuthContext | Promise<McpAuthContext>;

/**
 * Execute a tool handler with policy + safe error mapping.
 */
export async function runTool(
  toolName: string,
  contextLabel: string,
  extra: unknown,
  getAuthContext: AuthResolver | null,
  handler: (auth: McpAuthContext | null) => Promise<ToolResult>
): Promise<ToolResult> {
  try {
    const policy = getToolPolicy(toolName);
    let auth: McpAuthContext | null = null;

    if (policy.requiresAuth) {
      if (!getAuthContext) {
        throw new AuthenticationError(
          `Tool ${toolName} requires auth but no resolver is configured`
        );
      }
      auth = await Promise.resolve(getAuthContext(extra));
    }

    return await handler(auth);
  } catch (err: unknown) {
    return mapToolError(err, contextLabel);
  }
}

/**
 * Authenticated tools — auth is guaranteed non-null inside handler.
 */
export async function runAuthedTool(
  toolName: string,
  contextLabel: string,
  extra: unknown,
  getAuthContext: AuthResolver,
  handler: (auth: McpAuthContext) => Promise<ToolResult>
): Promise<ToolResult> {
  return runTool(toolName, contextLabel, extra, getAuthContext, async (auth) => {
    if (!auth) {
      throw new AuthenticationError();
    }
    return handler(auth);
  });
}

import { Account, Client } from "node-appwrite";
import type { AppwriteConfig } from "../infrastructure/appwrite/config.js";
import { type McpAuthContext, AuthenticationError } from "./context.js";

/**
 * Verify an Appwrite user JWT by calling Account.get() with setJWT.
 * The JWT is issued by Appwrite after client login (account.createJWT).
 */
export async function verifyAppwriteJwt(
  jwt: string,
  config: AppwriteConfig
): Promise<McpAuthContext> {
  const token = jwt.trim();
  if (!token) {
    throw new AuthenticationError("Empty JWT");
  }

  try {
    const client = new Client()
      .setEndpoint(config.endpoint)
      .setProject(config.projectId)
      .setJWT(token);

    const account = new Account(client);
    const user = await account.get();

    if (!user.$id) {
      throw new AuthenticationError("JWT válido pero sin user id");
    }

    return {
      userId: user.$id,
      userName: (user.name && user.name.trim()) || "Customer",
      userEmail: user.email ?? "",
    };
  } catch (err: unknown) {
    if (err instanceof AuthenticationError) throw err;
    const msg = err instanceof Error ? err.message : String(err);
    throw new AuthenticationError(
      `JWT Appwrite inválido o expirado: ${msg.slice(0, 120)}`
    );
  }
}

/** Extract Bearer token from Authorization header value */
export function extractBearerToken(
  authorization: string | null | undefined
): string | null {
  if (!authorization) return null;
  const m = /^Bearer\s+(.+)$/i.exec(authorization.trim());
  return m?.[1]?.trim() || null;
}

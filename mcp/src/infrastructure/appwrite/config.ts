/**
 * Infrastructure — Appwrite Config
 *
 * Centralized collection IDs and environment variable validation.
 * All Appwrite collection names are defined here — nowhere else.
 * Do NOT hardcode collection IDs in repository files.
 */

export const COLLECTIONS = {
  product: "product",
  category: "category",
  promotions: "promotions",
  sale: "sale",
  supportThreads: "support_threads",
  supportMessages: "support_messages",
} as const;

export type CollectionId = (typeof COLLECTIONS)[keyof typeof COLLECTIONS];

export interface AppwriteConfig {
  endpoint: string;
  projectId: string;
  apiKey: string;
  databaseId: string;
}

/**
 * Load and validate Appwrite configuration from environment variables.
 * Throws a clear error if any required variable is missing.
 *
 * Compatible with both Node.js (process.env) and Cloudflare Workers (env bindings).
 */
export function loadAppwriteConfig(
  env: Record<string, string | undefined>
): AppwriteConfig {
  const endpoint = env["APPWRITE_ENDPOINT"];
  const projectId = env["APPWRITE_PROJECT_ID"];
  const apiKey = env["APPWRITE_API_KEY"];
  const databaseId = env["APPWRITE_DATABASE_ID"];

  const missing: string[] = [];
  if (!endpoint) missing.push("APPWRITE_ENDPOINT");
  if (!projectId) missing.push("APPWRITE_PROJECT_ID");
  if (!apiKey) missing.push("APPWRITE_API_KEY");
  if (!databaseId) missing.push("APPWRITE_DATABASE_ID");

  if (missing.length > 0) {
    throw new Error(
      `Missing required Appwrite environment variables: ${missing.join(", ")}. ` +
        `Copy .dev.vars.example to .dev.vars and fill in your values.`
    );
  }

  return {
    endpoint: endpoint!,
    projectId: projectId!,
    apiKey: apiKey!,
    databaseId: databaseId!,
  };
}

import { Client, Databases, Users } from "node-appwrite";
import type { AppwriteConfig } from "./config.js";

/**
 * Infrastructure — Appwrite Client
 *
 * Creates and exports configured node-appwrite SDK instances.
 * Uses the SERVER-side SDK with an API Key — not the client browser SDK.
 *
 * Why node-appwrite and not the browser appwrite SDK:
 * - The MCP server is a backend service acting on behalf of customers
 * - It uses a scoped API Key, not browser sessions
 * - node-appwrite is fetch-based → compatible with Cloudflare Workers
 * - The Users API (for getUserById) requires a server-side API key
 */

export interface AppwriteClients {
  databases: Databases;
  users: Users;
  databaseId: string;
}

export function createAppwriteClients(config: AppwriteConfig): AppwriteClients {
  const client = new Client()
    .setEndpoint(config.endpoint)
    .setProject(config.projectId)
    .setKey(config.apiKey);

  return {
    databases: new Databases(client),
    users: new Users(client),
    databaseId: config.databaseId,
  };
}

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { registerAllTools, type ServicesContainer } from "./tools/index.js";
import { registerResources } from "./resources/index.js";
import { registerPrompts } from "./prompts/index.js";
import type { McpAuthContext } from "../auth/context.js";

/**
 * Creates and configures an instance of McpServer with all tools, resources, and prompts.
 */
export function createCustomerMcpServer(
  services: ServicesContainer,
  getAuthContext: (extra: unknown) => McpAuthContext
): McpServer {
  const server = new McpServer({
    name: "AlejoTaller Customer MCP Server",
    version: "0.1.0",
  });

  registerAllTools(server, services, getAuthContext);
  registerResources(server);
  registerPrompts(server);

  return server;
}

import type { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { ResourceTemplate } from "@modelcontextprotocol/sdk/server/mcp.js";

/**
 * Register MCP Resources — Exposed static or dynamic reference data.
 */
export function registerResources(server: McpServer): void {
  // Static resource: workshop contact info
  server.resource(
    "workshop-info",
    "info://workshop",
    async (uri) => ({
      contents: [
        {
          uri: uri.href,
          text: JSON.stringify(
            {
              name: "Taller Alejo",
              description: "Taller especializado en repuestos, servicios y atención personalizada.",
              supportEmail: "soporte@alejotaller.com",
              workingHours: "Lunes a Viernes, 8:00 AM - 5:00 PM",
              currencyOptions: ["CUP", "USD", "MLC"],
            },
            null,
            2
          ),
        },
      ],
    })
  );

  // Resource template: API policy documentation
  server.resource(
    "tool-policies",
    new ResourceTemplate("policy://tools/{toolName}", { list: undefined }),
    async (uri, { toolName }) => ({
      contents: [
        {
          uri: uri.href,
          text: `Politica para herramienta: ${toolName}. Requiere autorizacion previa del cliente para operaciones de escritura.`,
        },
      ],
    })
  );
}

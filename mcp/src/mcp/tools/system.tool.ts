import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { getToolPolicy } from "../../policies/tool-policy.js";

/**
 * System tools — Health check & server information.
 */
export function registerSystemTools(server: McpServer): void {
  // ─── ping_customer_mcp ──────────────────────────────────────────────────
  const pingPolicy = getToolPolicy("ping_customer_mcp");
  server.tool(
    "ping_customer_mcp",
    "Comprueba la conectividad y estado de salud del servidor MCP de AlejoTaller.",
    {},
    async (_args, _extra) => {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                status: "ok",
                server: "alejotaller-customer-mcp",
                version: "0.1.0",
                timestamp: new Date().toISOString(),
              },
              null,
              2
            ),
          },
        ],
      };
    }
  );

  // ─── get_server_info ───────────────────────────────────────────────────
  server.tool(
    "get_server_info",
    "Obtiene información general sobre las capacidades del servidor MCP de AlejoTaller y sus políticas de seguridad.",
    {},
    async (_args, _extra) => {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                name: "AlejoTaller Customer MCP Server",
                version: "0.1.0",
                description:
                  "Capa segura de interacción MCP entre agentes de IA y el área de clientes de AlejoTaller.",
                capabilities: [
                  "Consultar perfil de cliente",
                  "Consultar y cancelar pedidos/ventas",
                  "Descubrir catálogo de productos y categorías",
                  "Consultar promociones activas",
                  "Gestión de tickets y soporte al cliente",
                ],
                securityPolicy: {
                  authMode: "Header X-Customer-Id / MCP Context",
                  confirmationRequiredTools: [
                    "cancel_order",
                    "create_order",
                  ],
                },
              },
              null,
              2
            ),
          },
        ],
      };
    }
  );
}

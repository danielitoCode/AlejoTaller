import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { getToolPolicy } from "../../policies/tool-policy.js";

/**
 * System tools — Health check & server information.
 */
export function registerSystemTools(server: McpServer): void {
  getToolPolicy("ping_customer_mcp");

  server.tool(
    "ping_customer_mcp",
    "Comprueba la conectividad y estado de salud del servidor MCP de AlejoTaller (cliente B2C).",
    {},
    async (_args, _extra) => {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                status: "ok",
                server: "alejotaller-mcp",
                version: "0.1.0",
                scope: "b2c-customer",
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

  server.tool(
    "get_server_info",
    "Obtiene capacidades y políticas de seguridad del MCP cliente AlejoTaller.",
    {},
    async (_args, _extra) => {
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(
              {
                name: "AlejoTaller Customer MCP Server",
                worker: "alejotaller-mcp",
                version: "0.1.0",
                scope: "b2c-customer",
                description:
                  "Capa MCP segura entre agentes de IA y el área de clientes (B2C). No staff, no operador, no finance/movements.",
                capabilities: [
                  "Consultar perfil de cliente",
                  "Consultar y cancelar pedidos propios",
                  "Crear pedidos con soft-hold (requiresConfirmation)",
                  "Catálogo con available = max(0, existence − reserved)",
                  "Promociones activas",
                  "Tickets de soporte propios",
                ],
                notInScope: [
                  "Confirmación VERIFIED / operador",
                  "stock_movements / purchase_entry / sale_finance_event",
                  "Panel back-office",
                ],
                securityPolicy: {
                  authMode: "Header X-Customer-Id / MCP Context (JWT planned)",
                  confirmationRequiredTools: ["cancel_order", "create_order"],
                },
                transport: "streamable-http",
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

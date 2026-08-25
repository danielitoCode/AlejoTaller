import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { TOOL_POLICIES } from "../../policies/tool-policy.js";
import { okJson, runTool } from "./barrel.js";

/**
 * System tools — Health check & server information.
 */
export function registerSystemTools(server: McpServer): void {
  server.tool(
    "ping_customer_mcp",
    "Comprueba la conectividad y estado de salud del servidor MCP de AlejoTaller (cliente B2C).",
    {},
    async (_args, extra) =>
      runTool("ping_customer_mcp", "Ping MCP", extra, null, async () =>
        okJson({
          status: "ok",
          server: "alejotaller-mcp",
          version: "0.1.0",
          scope: "b2c-customer",
          timestamp: new Date().toISOString(),
        })
      )
  );

  server.tool(
    "get_server_info",
    "Obtiene capacidades y políticas de seguridad del MCP cliente AlejoTaller.",
    {},
    async (_args, extra) =>
      runTool("get_server_info", "Server info", extra, null, async () => {
        const confirmationRequiredTools = Object.entries(TOOL_POLICIES)
          .filter(([, p]) => p.requiresConfirmation)
          .map(([name]) => name);

        const writeTools = Object.entries(TOOL_POLICIES)
          .filter(([, p]) => p.permission === "WRITE")
          .map(([name]) => name);

        return okJson({
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
            confirmationRequiredTools,
            writeTools,
          },
          transport: "streamable-http",
        });
      })
  );
}

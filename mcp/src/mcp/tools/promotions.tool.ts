import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import type { PromotionService } from "../../services/promotion.service.js";

/**
 * Promotion tools — Active promotions catalog.
 */
export function registerPromotionTools(
  server: McpServer,
  promotionService: PromotionService
): void {
  // ─── list_active_promotions ──────────────────────────────────────────────
  server.tool(
    "list_active_promotions",
    "Obtiene la lista de promociones y ofertas vigentes de AlejoTaller (descuentos en productos y avisos especiales).",
    {},
    async (_args, _extra) => {
      try {
        const promotions = await promotionService.listActivePromotions();
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(promotions, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al listar promociones: ${message}` }],
        };
      }
    }
  );
}

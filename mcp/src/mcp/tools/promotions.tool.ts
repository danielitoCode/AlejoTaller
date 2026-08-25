import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import type { PromotionService } from "../../services/promotion.service.js";
import { okJson, runTool } from "./barrel.js";

/**
 * Promotion tools — catálogo público de promociones activas.
 */
export function registerPromotionTools(
  server: McpServer,
  promotionService: PromotionService
): void {
  server.tool(
    "list_active_promotions",
    "Lista promociones y ofertas vigentes de AlejoTaller.",
    {},
    async (_args, extra) =>
      runTool("list_active_promotions", "Listar promociones", extra, null, async () => {
        const promotions = await promotionService.listActivePromotions();
        return okJson(promotions);
      })
  );
}

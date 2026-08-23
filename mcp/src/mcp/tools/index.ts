import type { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import type { CustomerService } from "../../services/customer.service.js";
import type { OrderService } from "../../services/order.service.js";
import type { ProductService } from "../../services/product.service.js";
import type { CategoryService } from "../../services/category.service.js";
import type { PromotionService } from "../../services/promotion.service.js";
import type { SupportService } from "../../services/support.service.js";
import type { McpAuthContext } from "../../auth/context.js";

import { registerSystemTools } from "./system.tool.js";
import { registerCustomerTools } from "./customer.tool.js";
import { registerOrderTools } from "./orders.tool.js";
import { registerProductTools } from "./products.tool.js";
import { registerPromotionTools } from "./promotions.tool.js";
import { registerSupportTools } from "./support.tool.js";

export interface ServicesContainer {
  customerService: CustomerService;
  orderService: OrderService;
  productService: ProductService;
  categoryService: CategoryService;
  promotionService: PromotionService;
  supportService: SupportService;
}

export function registerAllTools(
  server: McpServer,
  services: ServicesContainer,
  getAuthContext: (extra: unknown) => McpAuthContext
): void {
  registerSystemTools(server);
  registerCustomerTools(server, services.customerService, getAuthContext);
  registerOrderTools(server, services.orderService, getAuthContext);
  registerProductTools(server, services.productService, services.categoryService);
  registerPromotionTools(server, services.promotionService);
  registerSupportTools(server, services.supportService, getAuthContext);
}

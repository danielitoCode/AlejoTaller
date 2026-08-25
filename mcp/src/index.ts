import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { loadAppwriteConfig } from "./infrastructure/appwrite/config.js";
import { createAppwriteClients } from "./infrastructure/appwrite/client.js";
import { AppwriteUserRepository } from "./infrastructure/appwrite/repositories/user.appwrite.repository.js";
import { AppwriteOrderRepository } from "./infrastructure/appwrite/repositories/order.appwrite.repository.js";
import { AppwriteProductRepository } from "./infrastructure/appwrite/repositories/product.appwrite.repository.js";
import { AppwriteCategoryRepository } from "./infrastructure/appwrite/repositories/category.appwrite.repository.js";
import { AppwritePromotionRepository } from "./infrastructure/appwrite/repositories/promotion.appwrite.repository.js";
import { AppwriteSupportRepository } from "./infrastructure/appwrite/repositories/support.appwrite.repository.js";

import { CustomerService } from "./services/customer.service.js";
import { OrderService } from "./services/order.service.js";
import { ProductService } from "./services/product.service.js";
import { CategoryService } from "./services/category.service.js";
import { PromotionService } from "./services/promotion.service.js";
import { SupportService } from "./services/support.service.js";

import { createCustomerMcpServer } from "./mcp/server.js";
import {
  parseAuthMode,
  resolveAuthContextFromMeta,
} from "./auth/resolver.js";

function asMetaRecord(value: unknown): Record<string, unknown> | undefined {
  if (value == null) return undefined;
  if (typeof value === "object" && !Array.isArray(value)) {
    return value as Record<string, unknown>;
  }
  return undefined;
}

async function main(): Promise<void> {
  console.error("Iniciando AlejoTaller Customer MCP Server (Modo local Stdio)...");

  let config;
  try {
    config = loadAppwriteConfig(
      process.env as Record<string, string | undefined>
    );
  } catch (err: unknown) {
    console.error(
      "Advertencia de configuracion Appwrite:",
      err instanceof Error ? err.message : err
    );
  }

  if (!config) {
    throw new Error("Se requiere configuracion Appwrite para iniciar el servidor.");
  }

  const clients = createAppwriteClients(config);
  const userRepo = new AppwriteUserRepository(clients.users);
  const orderRepo = new AppwriteOrderRepository(
    clients.databases,
    clients.databaseId
  );
  const productRepo = new AppwriteProductRepository(
    clients.databases,
    clients.databaseId
  );
  const categoryRepo = new AppwriteCategoryRepository(
    clients.databases,
    clients.databaseId
  );
  const promotionRepo = new AppwritePromotionRepository(
    clients.databases,
    clients.databaseId
  );
  const supportRepo = new AppwriteSupportRepository(
    clients.databases,
    clients.databaseId
  );

  const services = {
    customerService: new CustomerService(userRepo),
    orderService: new OrderService(orderRepo, productRepo),
    productService: new ProductService(productRepo),
    categoryService: new CategoryService(categoryRepo),
    promotionService: new PromotionService(promotionRepo),
    supportService: new SupportService(supportRepo),
  };

  const authMode = parseAuthMode(process.env["MCP_AUTH_MODE"]);

  const server = createCustomerMcpServer(services, async (extra: unknown) => {
    const rawMeta = (extra as { meta?: unknown } | null)?.meta;
    return resolveAuthContextFromMeta(asMetaRecord(rawMeta), {
      mode: authMode,
      appwriteConfig: config,
    });
  });

  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("Servidor MCP de AlejoTaller listo y conectado via stdio.");
}

main().catch((err: unknown) => {
  console.error("Error fatal en el servidor MCP:", err);
  process.exit(1);
});

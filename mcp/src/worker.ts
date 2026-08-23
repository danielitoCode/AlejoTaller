import { WebStandardStreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/webStandardStreamableHttp.js";
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
import { resolveAuthContext } from "./auth/resolver.js";

export interface Env {
  APPWRITE_ENDPOINT: string;
  APPWRITE_PROJECT_ID: string;
  APPWRITE_API_KEY: string;
  APPWRITE_DATABASE_ID: string;
  ENVIRONMENT?: string;
}

export default {
  async fetch(request: Request, env: Env, _ctx: ExecutionContext): Promise<Response> {
    // Handling CORS
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, X-Customer-Id, X-Customer-Name, X-Customer-Email",
        },
      });
    }

    try {
      const config = loadAppwriteConfig(env as unknown as Record<string, string | undefined>);
      const clients = createAppwriteClients(config);

      const userRepo = new AppwriteUserRepository(clients.users);
      const orderRepo = new AppwriteOrderRepository(clients.databases, clients.databaseId);
      const productRepo = new AppwriteProductRepository(clients.databases, clients.databaseId);
      const categoryRepo = new AppwriteCategoryRepository(clients.databases, clients.databaseId);
      const promotionRepo = new AppwritePromotionRepository(clients.databases, clients.databaseId);
      const supportRepo = new AppwriteSupportRepository(clients.databases, clients.databaseId);

      const services = {
        customerService: new CustomerService(userRepo),
        orderService: new OrderService(orderRepo),
        productService: new ProductService(productRepo),
        categoryService: new CategoryService(categoryRepo),
        promotionService: new PromotionService(promotionRepo),
        supportService: new SupportService(supportRepo),
      };

      // Extract headers for Auth context resolution
      const headersRecord: Record<string, string> = {};
      request.headers.forEach((val, key) => {
        headersRecord[key.toLowerCase()] = val;
      });

      const server = createCustomerMcpServer(services, () => {
        return resolveAuthContext(headersRecord);
      });

      // Streamable HTTP transport over Web Standards
      const transport = new WebStandardStreamableHTTPServerTransport();
      await server.connect(transport);

      return await transport.handleRequest(request);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err);
      return new Response(
        JSON.stringify({ error: message, status: "error" }),
        {
          status: 500,
          headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*",
          },
        }
      );
    }
  },
};

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

const CORS_HEADERS: Record<string, string> = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, DELETE, OPTIONS",
  "Access-Control-Allow-Headers":
    "Content-Type, Accept, Mcp-Session-Id, Last-Event-ID, X-Customer-Id, X-Customer-Name, X-Customer-Email, Authorization",
  "Access-Control-Expose-Headers": "Mcp-Session-Id",
};

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      ...CORS_HEADERS,
    },
  });
}

function healthPayload(env: Env) {
  const hasSecrets = Boolean(
    env.APPWRITE_ENDPOINT &&
      env.APPWRITE_PROJECT_ID &&
      env.APPWRITE_API_KEY &&
      env.APPWRITE_DATABASE_ID
  );
  return {
    status: hasSecrets ? "ok" : "degraded",
    worker: "alejotaller-mcp",
    version: "0.1.0",
    scope: "b2c-customer",
    transport: "streamable-http",
    environment: env.ENVIRONMENT ?? "unknown",
    appwriteConfigured: hasSecrets,
    timestamp: new Date().toISOString(),
    endpoints: {
      health: "GET /health",
      mcp: "POST / (Streamable HTTP MCP)",
    },
  };
}

export default {
  async fetch(request: Request, env: Env, _ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname.replace(/\/$/, "") || "/";

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    // §1 Health — no Appwrite round-trip; only checks secret bindings present
    if (request.method === "GET" && (path === "/health" || path === "/")) {
      return jsonResponse(healthPayload(env));
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

      const headersRecord: Record<string, string> = {};
      request.headers.forEach((val, key) => {
        headersRecord[key.toLowerCase()] = val;
      });

      const server = createCustomerMcpServer(services, () => {
        return resolveAuthContext(headersRecord);
      });

      const transport = new WebStandardStreamableHTTPServerTransport();
      await server.connect(transport);

      const mcpResponse = await transport.handleRequest(request);
      // Ensure CORS on MCP responses
      const headers = new Headers(mcpResponse.headers);
      for (const [k, v] of Object.entries(CORS_HEADERS)) {
        if (!headers.has(k)) headers.set(k, v);
      }
      return new Response(mcpResponse.body, {
        status: mcpResponse.status,
        statusText: mcpResponse.statusText,
        headers,
      });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err);
      return jsonResponse({ error: message, status: "error", worker: "alejotaller-mcp" }, 500);
    }
  },
};

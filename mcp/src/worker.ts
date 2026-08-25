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
import { parseAuthMode, resolveAuthContext } from "./auth/resolver.js";
import {
  buildCorsHeaders,
  isOriginAllowed,
  parseCorsOrigins,
  resolveAllowOrigin,
} from "./security/cors.js";
import {
  checkRateLimit,
  clientKeyFromRequest,
  parseRateLimitRpm,
} from "./security/rate-limit.js";

export interface Env {
  APPWRITE_ENDPOINT: string;
  APPWRITE_PROJECT_ID: string;
  APPWRITE_API_KEY: string;
  APPWRITE_DATABASE_ID: string;
  ENVIRONMENT?: string;
  /** Comma-separated origins; "*" = open (dev). Example: https://chat.example.com */
  MCP_CORS_ORIGINS?: string;
  /** header | jwt | jwt_or_header (default) */
  MCP_AUTH_MODE?: string;
  /** Requests per minute per client IP (default 60) */
  MCP_RATE_LIMIT_RPM?: string;
}

function jsonResponse(
  body: unknown,
  status: number,
  corsHeaders: Record<string, string>
): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      ...corsHeaders,
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
    authMode: parseAuthMode(env.MCP_AUTH_MODE),
    timestamp: new Date().toISOString(),
    endpoints: {
      health: "GET /health",
      mcp: "POST / (Streamable HTTP MCP)",
    },
  };
}

function headersToRecord(request: Request): Record<string, string> {
  const headersRecord: Record<string, string> = {};
  request.headers.forEach((val, key) => {
    headersRecord[key.toLowerCase()] = val;
  });
  return headersRecord;
}

export default {
  async fetch(
    request: Request,
    env: Env,
    _ctx: ExecutionContext
  ): Promise<Response> {
    const url = new URL(request.url);
    const path = url.pathname.replace(/\/$/, "") || "/";
    const requestOrigin = request.headers.get("Origin");
    const corsConfig = { allowedOrigins: parseCorsOrigins(env.MCP_CORS_ORIGINS) };
    const allowOrigin = resolveAllowOrigin(requestOrigin, corsConfig);
    const corsHeaders = buildCorsHeaders(allowOrigin);

    // Reject browser requests from non-allowlisted origins (non-browser OK)
    if (requestOrigin && !isOriginAllowed(requestOrigin, corsConfig)) {
      return jsonResponse(
        { error: "Origin not allowed", status: "forbidden", worker: "alejotaller-mcp" },
        403,
        buildCorsHeaders(null)
      );
    }

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: corsHeaders });
    }

    if (request.method === "GET" && (path === "/health" || path === "/")) {
      return jsonResponse(healthPayload(env), 200, corsHeaders);
    }

    // Rate limit MCP traffic (not health)
    const rpm = parseRateLimitRpm(env.MCP_RATE_LIMIT_RPM);
    const rl = checkRateLimit(clientKeyFromRequest(request), {
      maxRequests: rpm,
      windowMs: 60_000,
    });
    if (!rl.allowed) {
      return jsonResponse(
        {
          error: "Rate limit exceeded",
          status: "rate_limited",
          resetAt: new Date(rl.resetAt).toISOString(),
          worker: "alejotaller-mcp",
        },
        429,
        {
          ...corsHeaders,
          "Retry-After": String(Math.max(1, Math.ceil((rl.resetAt - Date.now()) / 1000))),
          "X-RateLimit-Remaining": "0",
        }
      );
    }

    try {
      const config = loadAppwriteConfig(
        env as unknown as Record<string, string | undefined>
      );
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

      const headersRecord = headersToRecord(request);
      const authMode = parseAuthMode(env.MCP_AUTH_MODE);

      const server = createCustomerMcpServer(services, async () => {
        return resolveAuthContext(headersRecord, {
          mode: authMode,
          appwriteConfig: config,
        });
      });

      const transport = new WebStandardStreamableHTTPServerTransport();
      await server.connect(transport);

      const mcpResponse = await transport.handleRequest(request);
      const headers = new Headers(mcpResponse.headers);
      for (const [k, v] of Object.entries(corsHeaders)) {
        if (!headers.has(k)) headers.set(k, v);
      }
      headers.set("X-RateLimit-Remaining", String(rl.remaining));

      return new Response(mcpResponse.body, {
        status: mcpResponse.status,
        statusText: mcpResponse.statusText,
        headers,
      });
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err);
      return jsonResponse(
        { error: message, status: "error", worker: "alejotaller-mcp" },
        500,
        corsHeaders
      );
    }
  },
};

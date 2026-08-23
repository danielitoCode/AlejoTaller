import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { OrderService } from "../../services/order.service.js";
import type { McpAuthContext } from "../../auth/context.js";
import type { Currency, DeliveryType } from "../../domain/order.js";

/**
 * Order tools — Manage customer orders (pedidos/ventas/reservas).
 */
export function registerOrderTools(
  server: McpServer,
  orderService: OrderService,
  getAuthContext: (extra: unknown) => McpAuthContext
): void {
  // ─── get_my_orders ──────────────────────────────────────────────────────
  server.tool(
    "get_my_orders",
    "Obtiene la lista de pedidos u órdenes del cliente autenticado actual (tanto pendientes como confirmados o cancelados).",
    {},
    async (_args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const orders = await orderService.getMyOrders(auth);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(orders, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al obtener pedidos: ${message}` }],
        };
      }
    }
  );

  // ─── get_order ──────────────────────────────────────────────────────────
  server.tool(
    "get_order",
    "Obtiene los detalles de un pedido específico por su ID. Solo permite ver pedidos del cliente autenticado.",
    {
      orderId: z.string().describe("Identificador único del pedido (ID de venta)"),
    },
    async (args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const order = await orderService.getOrder(auth, args.orderId);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(order, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al consultar el pedido: ${message}` }],
        };
      }
    }
  );

  // ─── cancel_order ───────────────────────────────────────────────────────
  server.tool(
    "cancel_order",
    "Cancela un pedido pendiente (UNVERIFIED) del cliente. Esta acción requiere confirmación previa por parte del usuario.",
    {
      orderId: z.string().describe("ID del pedido a cancelar"),
    },
    async (args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const cancelled = await orderService.cancelOrder(auth, args.orderId);
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  message: `El pedido ${args.orderId} ha sido cancelado con éxito.`,
                  order: cancelled,
                },
                null,
                2
              ),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al cancelar el pedido: ${message}` }],
        };
      }
    }
  );

  // ─── create_order ───────────────────────────────────────────────────────
  server.tool(
    "create_order",
    "Crea un nuevo pedido de productos para el cliente autenticado. Esta acción requiere confirmación previa.",
    {
      currency: z.enum(["CUP", "USD", "MLC"]).describe("Moneda de pago"),
      deliveryType: z.enum(["PICKUP", "DELIVERY"]).describe("Tipo de entrega: PICKUP (recoger) o DELIVERY (envío)"),
      items: z
        .array(
          z.object({
            productId: z.string().describe("ID del producto"),
            quantity: z.number().int().positive().describe("Cantidad deseada"),
          })
        )
        .min(1)
        .describe("Lista de productos y cantidades"),
      deliveryAddress: z
        .object({
          province: z.string(),
          municipality: z.string(),
          mainStreet: z.string(),
          betweenStreets: z.string().nullable().optional(),
          phone: z.string(),
          houseNumber: z.string(),
          referenceName: z.string().nullable().optional(),
        })
        .optional()
        .describe("Dirección de entrega (requerida si deliveryType es DELIVERY)"),
    },
    async (args, extra) => {
      try {
        const auth = getAuthContext(extra);
        const order = await orderService.createOrder(auth, {
          currency: args.currency as Currency,
          deliveryType: args.deliveryType as DeliveryType,
          items: args.items,
          deliveryAddress: args.deliveryAddress
            ? {
                province: args.deliveryAddress.province,
                municipality: args.deliveryAddress.municipality,
                mainStreet: args.deliveryAddress.mainStreet,
                betweenStreets: args.deliveryAddress.betweenStreets ?? null,
                phone: args.deliveryAddress.phone,
                houseNumber: args.deliveryAddress.houseNumber,
                referenceName: args.deliveryAddress.referenceName ?? null,
              }
            : undefined,
        });

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  message: "Pedido creado correctamente (pendiente de confirmación)",
                  order,
                },
                null,
                2
              ),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al crear el pedido: ${message}` }],
        };
      }
    }
  );
}

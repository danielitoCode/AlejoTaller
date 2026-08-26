import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { OrderService } from "../../services/order.service.js";
import type { McpAuthContext } from "../../auth/context.js";
import type { Currency, DeliveryType } from "../../domain/order.js";
import {
  okJson,
  confirmationHint,
  runAuthedTool,
  type AuthResolver,
} from "./barrel.js";

const deliveryAddressSchema = z.object({
  province: z.string().min(1),
  municipality: z.string().min(1),
  mainStreet: z.string().min(1),
  betweenStreets: z.string().nullable().optional(),
  phone: z.string().min(1),
  houseNumber: z.string().min(1),
  referenceName: z.string().nullable().optional(),
});

const createOrderSchema = {
  currency: z.enum(["CUP", "USD", "MLC"]).describe("Moneda de pago"),
  deliveryType: z
    .enum(["PICKUP", "DELIVERY"])
    .describe("Tipo de entrega: PICKUP (recoger) o DELIVERY (envío)"),
  items: z
    .array(
      z.object({
        productId: z.string().min(1).describe("ID del producto"),
        quantity: z.number().int().positive().describe("Cantidad deseada"),
      })
    )
    .min(1)
    .describe("Lista de productos y cantidades"),
  deliveryAddress: deliveryAddressSchema
    .optional()
    .describe("Dirección de entrega (requerida si deliveryType es DELIVERY)"),
};

/**
 * Order tools — pedidos con soft-hold y ownership B2C.
 */
export function registerOrderTools(
  server: McpServer,
  orderService: OrderService,
  getAuthContext: AuthResolver
): void {
  server.tool(
    "get_my_orders",
    [
      "Obtiene EXCLUSIVAMENTE los pedidos/reservas/compras del cliente autenticado dentro de AlejoTaller",
      "(pendientes UNVERIFIED, confirmados o cancelados).",
      "",
      "Úsala cuando el usuario pregunte por:",
      "- mis pedidos / mis compras / mis órdenes",
      "- mis reservas (en AlejoTaller = pedidos, NO vuelos ni hoteles)",
      "- qué he comprado / pedidos pendientes / estado de mi pedido",
      "",
      "NO representa reservas de vuelos, hoteles, restaurantes, transporte ni servicios externos.",
    ].join("\n"),
    {},
    async (_args, extra) =>
      runAuthedTool("get_my_orders", "Obtener pedidos", extra, getAuthContext, async (auth) => {
        const orders = await orderService.getMyOrders(auth);
        return okJson(orders);
      })
  );

  server.tool(
    "get_order",
    [
      "Obtiene los detalles de UN pedido/reserva de AlejoTaller por ID.",
      "Solo pedidos del cliente autenticado.",
      "Úsala cuando el usuario dé un ID concreto de pedido o quiera el detalle de una reserva/compra en el taller.",
    ].join("\n"),
    {
      orderId: z.string().min(1).describe("Identificador único del pedido (ID de venta)"),
    },
    async (args, extra) =>
      runAuthedTool("get_order", "Consultar pedido", extra, getAuthContext, async (auth) => {
        const order = await orderService.getOrder(auth, args.orderId);
        return okJson(order);
      })
  );

  server.tool(
    "cancel_order",
    [
      "Cancela un pedido/reserva pendiente (UNVERIFIED) del cliente en AlejoTaller y libera el soft-hold de stock.",
      "Úsala cuando el usuario pida cancelar un pedido o una reserva del taller.",
    ].join("\n") + confirmationHint("cancel_order"),
    {
      orderId: z.string().min(1).describe("ID del pedido a cancelar"),
    },
    async (args, extra) =>
      runAuthedTool("cancel_order", "Cancelar pedido", extra, getAuthContext, async (auth) => {
        const cancelled = await orderService.cancelOrder(auth, args.orderId);
        return okJson({
          message: `El pedido ${args.orderId} ha sido cancelado con éxito.`,
          order: cancelled,
        });
      })
  );

  server.tool(
    "create_order",
    [
      "Crea un pedido/reserva UNVERIFIED con soft-hold de stock para el cliente en AlejoTaller.",
      "Úsala cuando el usuario quiera comprar, reservar o pedir productos del taller.",
      "No es para reservas de vuelos, hoteles ni servicios externos.",
    ].join("\n") + confirmationHint("create_order"),
    createOrderSchema,
    async (args, extra) =>
      runAuthedTool("create_order", "Crear pedido", extra, getAuthContext, async (auth: McpAuthContext) => {
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

        return okJson({
          message: "Pedido creado correctamente (pendiente de confirmación del taller).",
          order,
        });
      })
  );
}

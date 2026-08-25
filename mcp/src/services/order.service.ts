import type { IOrderRepository } from "../repositories/order.repository.js";
import type { IProductRepository } from "../repositories/product.repository.js";
import type {
  Order,
  OrderItem,
  CreateOrderInput,
} from "../domain/order.js";
import { availableStock } from "../domain/product.js";
import type { McpAuthContext } from "../auth/context.js";
import { AuthenticationError, AuthorizationError } from "../auth/context.js";

/**
 * Service — Order
 *
 * Soft-hold flow mirrors web:
 * - RegisterNewSaleCaseUse (create + atomic reserved++)
 * - CancelUnverifiedSaleCaseUse + ReleaseSoftHoldCaseUse (DELETED + reserved--)
 */
export class OrderService {
  constructor(
    private readonly orderRepo: IOrderRepository,
    private readonly productRepo: IProductRepository
  ) {}

  async getMyOrders(auth: McpAuthContext): Promise<Order[]> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    return await this.orderRepo.listByUser(auth.userId);
  }

  async getOrder(auth: McpAuthContext, orderId: string): Promise<Order> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    const order = await this.orderRepo.getById(orderId);
    if (!order) {
      throw new Error(`Order not found: ${orderId}`);
    }
    if (order.userId !== auth.userId) {
      throw new AuthorizationError("You are not authorized to view this order.");
    }
    return order;
  }

  /**
   * Create UNVERIFIED order + soft-hold.
   * 1) Resolve products & validate available stock
   * 2) Persist sale (stock_hold_applied=false)
   * 3) Atomic incrementReserved per line (compensate on failure)
   * 4) Flag stock_hold_applied=true
   */
  async createOrder(
    auth: McpAuthContext,
    input: CreateOrderInput
  ): Promise<Order> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    if (!input.items || input.items.length === 0) {
      throw new Error("Cannot create order with no items.");
    }
    if (input.deliveryType === "DELIVERY" && !input.deliveryAddress) {
      throw new Error("deliveryAddress is required when deliveryType is DELIVERY");
    }

    const resolved = await this.resolveAndValidateLines(input.items);

    const created = await this.orderRepo.create(auth.userId, {
      items: resolved.items,
      totalAmount: resolved.totalAmount,
      currency: input.currency,
      deliveryType: input.deliveryType,
      deliveryAddress: input.deliveryAddress,
    });

    let holdApplied = false;
    let holdError: string | null = null;

    try {
      await this.applySoftHold(created);
      holdApplied = true;
    } catch (error) {
      holdError = error instanceof Error ? error.message : String(error);
      console.error(
        `[OrderService] soft_hold_failure saleId=${created.id}: ${holdError}`
      );
    }

    let result: Order = { ...created, stockHoldApplied: holdApplied };

    if (holdApplied) {
      try {
        result = await this.orderRepo.updateStockHoldApplied(created.id, true);
      } catch (flagErr) {
        console.warn(
          `[OrderService] stock_hold_applied flag failed saleId=${created.id}:`,
          flagErr instanceof Error ? flagErr.message : String(flagErr)
        );
        result = { ...result, stockHoldApplied: true };
      }
    } else if (holdError) {
      // Sale exists but hold failed — surface error; agent/ops can recover.
      // Prefer hard fail so the agent does not treat stock as reserved.
      throw new Error(
        `Pedido creado (${created.id}) pero el soft-hold falló: ${holdError}. ` +
          `El pedido quedó sin reserva de stock; cancélalo o reintenta.`
      );
    }

    return result;
  }

  /**
   * Cancel own UNVERIFIED order: DELETED + release reserved.
   */
  async cancelOrder(auth: McpAuthContext, orderId: string): Promise<Order> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }

    const order = await this.orderRepo.getById(orderId);
    if (!order) {
      throw new Error(`Order not found: ${orderId}`);
    }
    if (order.userId !== auth.userId) {
      throw new AuthorizationError("You are not authorized to cancel this order.");
    }
    if (order.status !== "UNVERIFIED") {
      throw new Error(
        `Solo se pueden cancelar pedidos pendientes (UNVERIFIED). Estado actual: ${order.status}`
      );
    }

    const updated = await this.orderRepo.cancel(orderId);

    try {
      await this.releaseSoftHold(order);
    } catch (e) {
      console.error(
        `[OrderService] release soft-hold failed saleId=${order.id}:`,
        e instanceof Error ? e.message : String(e)
      );
    }

    try {
      return await this.orderRepo.updateStockHoldApplied(orderId, false);
    } catch {
      return { ...updated, stockHoldApplied: false };
    }
  }

  // ─── Soft-hold helpers ───────────────────────────────────────────────────

  private async resolveAndValidateLines(
    lines: Array<{ productId: string; quantity: number }>
  ): Promise<{ items: OrderItem[]; totalAmount: number }> {
    const items: OrderItem[] = [];
    let totalAmount = 0;

    for (const line of lines) {
      const qty = Math.floor(line.quantity);
      if (!line.productId || qty <= 0) {
        throw new Error(
          `Línea inválida: productId y quantity >= 1 requeridos (got ${line.productId}, ${line.quantity})`
        );
      }

      const product =
        (await this.productRepo.refreshFromRemote(line.productId)) ??
        (await this.productRepo.getById(line.productId));

      if (!product) {
        throw new Error(`Producto no disponible: ${line.productId}`);
      }

      const available = availableStock(product);
      if (qty > available) {
        throw new Error(
          `No hay disponibilidad para "${product.name}" ` +
            `(pedido=${qty}, disponible=${available})`
        );
      }

      const unit = Number(product.price) || 0;
      items.push({
        productId: product.id,
        productName: product.name,
        quantity: qty,
        unitPrice: unit,
        listUnitPrice: unit,
        price: unit,
      });
      totalAmount += unit * qty;
    }

    return { items, totalAmount };
  }

  private async applySoftHold(sale: Order): Promise<void> {
    if (sale.stockHoldApplied) return;
    if (!sale.items?.length) {
      throw new Error("Sale sin líneas: no hay soft-hold que aplicar");
    }

    const touched = new Map<string, number>();

    try {
      for (const item of sale.items) {
        const product =
          (await this.productRepo.refreshFromRemote(item.productId)) ??
          (await this.productRepo.getById(item.productId));

        if (!product) {
          throw new Error(
            `Soft-hold: producto no disponible productId=${item.productId}`
          );
        }

        const available = availableStock(product);
        if (item.quantity > available) {
          throw new Error(
            `Stock insuficiente (concurrencia): ${item.productName ?? item.productId} ` +
              `(pedido=${item.quantity}, disponible=${available})`
          );
        }

        const maxReserved = Math.max(0, Math.floor(product.existence));
        const updated = await this.productRepo.incrementReserved(
          item.productId,
          item.quantity,
          maxReserved
        );

        if (!updated) {
          throw new Error(
            `Soft-hold atómico rechazado productId=${item.productId}`
          );
        }

        touched.set(
          item.productId,
          (touched.get(item.productId) ?? 0) + item.quantity
        );
      }
    } catch (error) {
      await this.compensateSoftHold(touched, sale.id);
      throw error;
    }
  }

  private async compensateSoftHold(
    quantities: Map<string, number>,
    saleId: string
  ): Promise<void> {
    for (const [productId, quantity] of [...quantities.entries()].reverse()) {
      try {
        const released = await this.productRepo.decrementReserved(
          productId,
          quantity
        );
        if (!released) {
          console.error(
            `[OrderService] soft_hold_compensation_failed saleId=${saleId} productId=${productId}`
          );
        } else {
          console.warn(
            `[OrderService] soft_hold_compensated saleId=${saleId} productId=${productId} qty=${quantity}`
          );
        }
      } catch (error) {
        console.error(
          `[OrderService] soft_hold_compensation_failed saleId=${saleId} productId=${productId}`,
          error
        );
      }
    }
  }

  private async releaseSoftHold(sale: Order): Promise<void> {
    for (const item of sale.items ?? []) {
      const qty = Math.max(0, Math.floor(item.quantity));
      if (qty === 0 || !item.productId) continue;

      const updated = await this.productRepo.decrementReserved(
        item.productId,
        qty
      );
      if (!updated) {
        console.warn(
          `[OrderService] decremento atómico falló productId=${item.productId} saleId=${sale.id}`
        );
      }
    }
  }
}

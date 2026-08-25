import type {
  Order,
  CreateOrderInput,
  OrderStatus,
} from "../domain/order.js";

/**
 * Repository interface — Order
 *
 * Maps to the `sale` Appwrite collection.
 * Soft-hold application lives in OrderService (Fase 2), using IProductRepository
 * atomic reserved ops from Fase 1.
 */
export interface IOrderRepository {
  /** List all orders belonging to a specific user */
  listByUser(userId: string): Promise<Order[]>;

  /** Get a single order by its Appwrite document ID */
  getById(orderId: string): Promise<Order | null>;

  /**
   * Persist a new UNVERIFIED sale document.
   * Does not apply soft-hold by itself — OrderService coordinates hold + flag.
   */
  create(userId: string, input: CreateOrderInput): Promise<Order>;

  /**
   * Update buy_state (e.g. UNVERIFIED → DELETED for client cancel).
   * Client MCP must never set VERIFIED.
   */
  updateVerified(orderId: string, status: OrderStatus): Promise<Order>;

  /** Mark stock_hold_applied after successful reserved increments */
  updateStockHoldApplied(orderId: string, value: boolean): Promise<Order>;

  /**
   * Cancel an UNVERIFIED order (sets status → DELETED).
   * Soft-hold release is coordinated by OrderService (Fase 2).
   */
  cancel(orderId: string): Promise<Order>;
}

import type {
  Order,
  CreateOrderPersistInput,
  OrderStatus,
} from "../domain/order.js";

/**
 * Repository interface — Order (`sale` collection).
 * Soft-hold coordination lives in OrderService + IProductRepository.
 */
export interface IOrderRepository {
  listByUser(userId: string): Promise<Order[]>;

  getById(orderId: string): Promise<Order | null>;

  /**
   * Persist UNVERIFIED sale with enriched lines and amount.
   * stock_hold_applied starts false; service sets true after hold.
   */
  create(userId: string, input: CreateOrderPersistInput): Promise<Order>;

  /** Client may only set DELETED (never VERIFIED). */
  updateVerified(orderId: string, status: OrderStatus): Promise<Order>;

  updateStockHoldApplied(orderId: string, value: boolean): Promise<Order>;

  /**
   * Sets buy_state DELETED if currently UNVERIFIED.
   * Does not release reserved — OrderService does.
   */
  cancel(orderId: string): Promise<Order>;
}

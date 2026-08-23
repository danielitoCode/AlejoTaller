import type { Order, CreateOrderInput } from "../domain/order.js";

/**
 * Repository interface — Order
 *
 * Maps to the `sale` Appwrite collection.
 * Implementations: AppwriteOrderRepository, MockOrderRepository (tests)
 */
export interface IOrderRepository {
  /** List all orders belonging to a specific user */
  listByUser(userId: string): Promise<Order[]>;

  /** Get a single order by its Appwrite document ID */
  getById(orderId: string): Promise<Order | null>;

  /**
   * Create a new UNVERIFIED order.
   * Stock soft-hold is applied atomically in the Appwrite implementation.
   *
   * TODO (Phase 2): Full stock validation + atomic reserved increment.
   * Currently creates the document; stock hold is best-effort.
   */
  create(userId: string, input: CreateOrderInput): Promise<Order>;

  /**
   * Cancel an UNVERIFIED order (sets status → DELETED).
   * Returns the updated order.
   * Throws if the order is already VERIFIED or DELETED.
   */
  cancel(orderId: string): Promise<Order>;
}

import type { IOrderRepository } from "../repositories/order.repository.js";
import type { Order, CreateOrderInput } from "../domain/order.js";
import type { McpAuthContext } from "../auth/context.js";
import { AuthenticationError, AuthorizationError } from "../auth/context.js";

/**
 * Service — Order
 * Business logic for customer orders.
 */
export class OrderService {
  constructor(private readonly orderRepo: IOrderRepository) {}

  /**
   * Get all orders belonging to authenticated user.
   */
  async getMyOrders(auth: McpAuthContext): Promise<Order[]> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    return await this.orderRepo.listByUser(auth.userId);
  }

  /**
   * Get specific order by ID, ensuring user ownership.
   */
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
   * Create a new order for authenticated user.
   */
  async createOrder(auth: McpAuthContext, input: CreateOrderInput): Promise<Order> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    if (!input.items || input.items.length === 0) {
      throw new Error("Cannot create order with no items.");
    }
    return await this.orderRepo.create(auth.userId, input);
  }

  /**
   * Cancel order belonging to authenticated user.
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

    return await this.orderRepo.cancel(orderId);
  }
}

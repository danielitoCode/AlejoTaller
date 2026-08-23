import { describe, it, expect, vi } from "vitest";
import { OrderService } from "../../src/services/order.service.js";
import type { IOrderRepository } from "../../src/repositories/order.repository.js";
import type { Order } from "../../src/domain/order.js";
import type { McpAuthContext } from "../../src/auth/context.js";
import { AuthorizationError } from "../../src/auth/context.js";

describe("OrderService Authorization", () => {
  const mockOrderUserA: Order = {
    id: "ord-100",
    date: new Date().toISOString(),
    totalAmount: 1500,
    status: "UNVERIFIED",
    items: [],
    currency: "CUP",
    userId: "user-A",
    deliveryType: "PICKUP",
    deliveryAddress: null,
    orderType: null,
    stockHoldApplied: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  const mockOrderRepo: IOrderRepository = {
    listByUser: vi.fn().mockImplementation(async (userId) => {
      return userId === "user-A" ? [mockOrderUserA] : [];
    }),
    getById: vi.fn().mockImplementation(async (orderId) => {
      return orderId === "ord-100" ? mockOrderUserA : null;
    }),
    create: vi.fn(),
    cancel: vi.fn().mockImplementation(async (orderId) => ({
      ...mockOrderUserA,
      status: "DELETED",
    })),
  };

  const service = new OrderService(mockOrderRepo);

  it("User A should be able to view their own order", async () => {
    const authUserA: McpAuthContext = {
      userId: "user-A",
      userName: "User A",
      userEmail: "a@test.com",
    };

    const order = await service.getOrder(authUserA, "ord-100");
    expect(order.id).toBe("ord-100");
    expect(order.userId).toBe("user-A");
  });

  it("User B should NOT be able to view User A's order", async () => {
    const authUserB: McpAuthContext = {
      userId: "user-B",
      userName: "User B",
      userEmail: "b@test.com",
    };

    await expect(service.getOrder(authUserB, "ord-100")).rejects.toThrow(
      AuthorizationError
    );
  });

  it("User B should NOT be able to cancel User A's order", async () => {
    const authUserB: McpAuthContext = {
      userId: "user-B",
      userName: "User B",
      userEmail: "b@test.com",
    };

    await expect(service.cancelOrder(authUserB, "ord-100")).rejects.toThrow(
      AuthorizationError
    );
  });
});

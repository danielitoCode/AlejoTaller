import { describe, it, expect, vi, beforeEach } from "vitest";
import { OrderService } from "../../src/services/order.service.js";
import type { IOrderRepository } from "../../src/repositories/order.repository.js";
import type { IProductRepository } from "../../src/repositories/product.repository.js";
import type { Order } from "../../src/domain/order.js";
import type { Product } from "../../src/domain/product.js";
import type { McpAuthContext } from "../../src/auth/context.js";
import { AuthorizationError } from "../../src/auth/context.js";

const authA: McpAuthContext = {
  userId: "user-A",
  userName: "User A",
  userEmail: "a@test.com",
};

const authB: McpAuthContext = {
  userId: "user-B",
  userName: "User B",
  userEmail: "b@test.com",
};

function makeProduct(partial: Partial<Product> & Pick<Product, "id">): Product {
  return {
    name: "Prod",
    description: "",
    existence: 10,
    reserved: 0,
    price: 100,
    photoUrl: "",
    categoryId: "c1",
    rating: 0,
    createdAt: null,
    ...partial,
  };
}

describe("OrderService Authorization", () => {
  const mockOrderUserA: Order = {
    id: "ord-100",
    date: new Date().toISOString(),
    totalAmount: 1500,
    status: "UNVERIFIED",
    items: [{ productId: "p1", productName: "X", quantity: 1, unitPrice: 100, listUnitPrice: 100 }],
    currency: "CUP",
    userId: "user-A",
    deliveryType: "PICKUP",
    deliveryAddress: null,
    orderType: null,
    stockHoldApplied: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  const orderRepo: IOrderRepository = {
    listByUser: vi.fn().mockImplementation(async (userId) =>
      userId === "user-A" ? [mockOrderUserA] : []
    ),
    getById: vi.fn().mockImplementation(async (orderId) =>
      orderId === "ord-100" ? mockOrderUserA : null
    ),
    create: vi.fn(),
    updateVerified: vi.fn(),
    updateStockHoldApplied: vi.fn(),
    cancel: vi.fn().mockResolvedValue({ ...mockOrderUserA, status: "DELETED" }),
  };

  const productRepo: IProductRepository = {
    listAll: vi.fn(),
    getById: vi.fn(),
    refreshFromRemote: vi.fn(),
    listByCategory: vi.fn(),
    incrementReserved: vi.fn(),
    decrementReserved: vi.fn().mockResolvedValue(makeProduct({ id: "p1", reserved: 0 })),
  };

  const service = new OrderService(orderRepo, productRepo);

  it("User A can view own order", async () => {
    const order = await service.getOrder(authA, "ord-100");
    expect(order.userId).toBe("user-A");
  });

  it("User B cannot view User A order", async () => {
    await expect(service.getOrder(authB, "ord-100")).rejects.toThrow(AuthorizationError);
  });

  it("User B cannot cancel User A order", async () => {
    await expect(service.cancelOrder(authB, "ord-100")).rejects.toThrow(AuthorizationError);
  });
});

describe("OrderService soft-hold create/cancel", () => {
  let orderRepo: IOrderRepository;
  let productRepo: IProductRepository;
  let products: Map<string, Product>;

  beforeEach(() => {
    products = new Map([
      ["p1", makeProduct({ id: "p1", name: "Filtro", existence: 10, reserved: 0, price: 50 })],
      ["p2", makeProduct({ id: "p2", name: "Aceite", existence: 5, reserved: 0, price: 80 })],
    ]);

    productRepo = {
      listAll: vi.fn(),
      listByCategory: vi.fn(),
      getById: vi.fn(async (id) => products.get(id) ?? null),
      refreshFromRemote: vi.fn(async (id) => products.get(id) ?? null),
      incrementReserved: vi.fn(async (id, qty, maxReserved) => {
        const p = products.get(id);
        if (!p) return null;
        if (p.reserved + qty > maxReserved) return null;
        const next = { ...p, reserved: p.reserved + qty };
        products.set(id, next);
        return next;
      }),
      decrementReserved: vi.fn(async (id, qty) => {
        const p = products.get(id);
        if (!p) return null;
        const next = { ...p, reserved: Math.max(0, p.reserved - qty) };
        products.set(id, next);
        return next;
      }),
    };

    orderRepo = {
      listByUser: vi.fn(),
      getById: vi.fn(),
      create: vi.fn(async (userId, input) => ({
        id: "ord-new",
        date: new Date().toISOString(),
        totalAmount: input.totalAmount,
        status: "UNVERIFIED" as const,
        items: input.items,
        currency: input.currency,
        userId,
        deliveryType: input.deliveryType,
        deliveryAddress: input.deliveryAddress ?? null,
        orderType: null,
        stockHoldApplied: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      })),
      updateVerified: vi.fn(),
      updateStockHoldApplied: vi.fn(async (id, value) => ({
        id,
        date: new Date().toISOString(),
        totalAmount: 50,
        status: "UNVERIFIED" as const,
        items: [
          {
            productId: "p1",
            productName: "Filtro",
            quantity: 2,
            unitPrice: 50,
            listUnitPrice: 50,
          },
        ],
        currency: "CUP" as const,
        userId: "user-A",
        deliveryType: "PICKUP" as const,
        deliveryAddress: null,
        orderType: null,
        stockHoldApplied: value,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      })),
      cancel: vi.fn(async (id) => ({
        id,
        date: new Date().toISOString(),
        totalAmount: 50,
        status: "DELETED" as const,
        items: [
          {
            productId: "p1",
            productName: "Filtro",
            quantity: 2,
            unitPrice: 50,
            listUnitPrice: 50,
          },
        ],
        currency: "CUP" as const,
        userId: "user-A",
        deliveryType: "PICKUP" as const,
        deliveryAddress: null,
        orderType: null,
        stockHoldApplied: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      })),
    };
  });

  it("createOrder increments reserved and sets stockHoldApplied", async () => {
    const service = new OrderService(orderRepo, productRepo);
    const order = await service.createOrder(authA, {
      currency: "CUP",
      deliveryType: "PICKUP",
      items: [{ productId: "p1", quantity: 2 }],
    });

    expect(order.stockHoldApplied).toBe(true);
    expect(productRepo.incrementReserved).toHaveBeenCalledWith("p1", 2, 10);
    expect(products.get("p1")!.reserved).toBe(2);
    expect(orderRepo.updateStockHoldApplied).toHaveBeenCalledWith("ord-new", true);
  });

  it("createOrder rejects insufficient stock before create", async () => {
    products.set("p1", makeProduct({ id: "p1", existence: 1, reserved: 0 }));
    const service = new OrderService(orderRepo, productRepo);

    await expect(
      service.createOrder(authA, {
        currency: "CUP",
        deliveryType: "PICKUP",
        items: [{ productId: "p1", quantity: 5 }],
      })
    ).rejects.toThrow(/disponibilidad/i);

    expect(orderRepo.create).not.toHaveBeenCalled();
  });

  it("createOrder compensates previous lines when a later hold fails", async () => {
    productRepo.incrementReserved = vi.fn(async (id, qty, max) => {
      if (id === "p2") return null;
      const p = products.get(id)!;
      if (p.reserved + qty > max) return null;
      const next = { ...p, reserved: p.reserved + qty };
      products.set(id, next);
      return next;
    });

    const service = new OrderService(orderRepo, productRepo);

    await expect(
      service.createOrder(authA, {
        currency: "CUP",
        deliveryType: "PICKUP",
        items: [
          { productId: "p1", quantity: 2 },
          { productId: "p2", quantity: 1 },
        ],
      })
    ).rejects.toThrow(/soft-hold/i);

    expect(productRepo.decrementReserved).toHaveBeenCalledWith("p1", 2);
    expect(products.get("p1")!.reserved).toBe(0);
  });

  it("cancelOrder releases reserved", async () => {
    products.set("p1", makeProduct({ id: "p1", reserved: 2 }));
    const existing: Order = {
      id: "ord-100",
      date: new Date().toISOString(),
      totalAmount: 100,
      status: "UNVERIFIED",
      items: [
        {
          productId: "p1",
          productName: "Filtro",
          quantity: 2,
          unitPrice: 50,
          listUnitPrice: 50,
        },
      ],
      currency: "CUP",
      userId: "user-A",
      deliveryType: "PICKUP",
      deliveryAddress: null,
      orderType: null,
      stockHoldApplied: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    orderRepo.getById = vi.fn().mockResolvedValue(existing);

    const service = new OrderService(orderRepo, productRepo);
    await service.cancelOrder(authA, "ord-100");

    expect(orderRepo.cancel).toHaveBeenCalledWith("ord-100");
    expect(productRepo.decrementReserved).toHaveBeenCalledWith("p1", 2);
    expect(products.get("p1")!.reserved).toBe(0);
  });
});

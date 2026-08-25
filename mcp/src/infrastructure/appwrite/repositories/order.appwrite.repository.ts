import { type Databases, ID, Query } from "node-appwrite";
import type { IOrderRepository } from "../../../repositories/order.repository.js";
import type {
  Order,
  OrderItem,
  DeliveryAddress,
  CreateOrderInput,
  OrderStatus,
  OrderType,
  Currency,
  DeliveryType,
} from "../../../domain/order.js";
import { COLLECTIONS } from "../config.js";

/**
 * Appwrite implementation of IOrderRepository.
 * Collection schema: date, amount, buy_state, currency, products (JSON),
 * user_id, delivery_type, delivery_address (JSON), sale_type, stock_hold_applied
 */
export class AppwriteOrderRepository implements IOrderRepository {
  constructor(
    private readonly databases: Databases,
    private readonly databaseId: string
  ) {}

  async listByUser(userId: string): Promise<Order[]> {
    const res = await this.databases.listDocuments(
      this.databaseId,
      COLLECTIONS.sale,
      [
        Query.equal("user_id", userId),
        Query.orderDesc("$createdAt"),
        Query.limit(100),
      ]
    );
    return res.documents.map((d) =>
      this.toOrder(d as unknown as AppwriteSaleDoc)
    );
  }

  async getById(orderId: string): Promise<Order | null> {
    try {
      const doc = await this.databases.getDocument(
        this.databaseId,
        COLLECTIONS.sale,
        orderId
      );
      return this.toOrder(doc as unknown as AppwriteSaleDoc);
    } catch {
      return null;
    }
  }

  async create(userId: string, input: CreateOrderInput): Promise<Order> {
    const items: OrderItem[] = input.items.map((i) => ({
      productId: i.productId,
      productName: null,
      quantity: i.quantity,
      unitPrice: null,
      listUnitPrice: null,
    }));

    const payload: Record<string, unknown> = {
      date: new Date().toISOString(),
      amount: 0,
      buy_state: "UNVERIFIED",
      currency: input.currency,
      products: JSON.stringify(items),
      user_id: userId,
      delivery_type: input.deliveryType,
      delivery_address: input.deliveryAddress
        ? JSON.stringify(input.deliveryAddress)
        : null,
      sale_type: null,
      stock_hold_applied: false,
    };

    const doc = await this.databases.createDocument(
      this.databaseId,
      COLLECTIONS.sale,
      ID.unique(),
      payload
    );

    return this.toOrder(doc as unknown as AppwriteSaleDoc);
  }

  async updateVerified(orderId: string, status: OrderStatus): Promise<Order> {
    if (status === "VERIFIED") {
      throw new Error(
        "MCP B2C no puede marcar pedidos como VERIFIED (solo operador/staff)"
      );
    }

    const doc = await this.databases.updateDocument(
      this.databaseId,
      COLLECTIONS.sale,
      orderId,
      { buy_state: status }
    );

    return this.toOrder(doc as unknown as AppwriteSaleDoc);
  }

  async updateStockHoldApplied(
    orderId: string,
    value: boolean
  ): Promise<Order> {
    const doc = await this.databases.updateDocument(
      this.databaseId,
      COLLECTIONS.sale,
      orderId,
      { stock_hold_applied: value }
    );

    return this.toOrder(doc as unknown as AppwriteSaleDoc);
  }

  async cancel(orderId: string): Promise<Order> {
    const existing = await this.getById(orderId);
    if (!existing) {
      throw new Error(`Order not found: ${orderId}`);
    }
    if (existing.status !== "UNVERIFIED") {
      throw new Error(
        `Only pending orders (UNVERIFIED) can be cancelled. Current status: ${existing.status}`
      );
    }

    return this.updateVerified(orderId, "DELETED");
  }

  private toOrder(doc: AppwriteSaleDoc): Order {
    let items: OrderItem[] = [];
    try {
      const parsed = JSON.parse(doc.products ?? "[]") as unknown[];
      items = (parsed as RawSaleItem[]).map((i) => ({
        productId: i.productId ?? "",
        productName: i.productName ?? null,
        quantity: i.quantity ?? 0,
        unitPrice: i.unitPrice ?? i.price ?? null,
        listUnitPrice: i.listUnitPrice ?? null,
      }));
    } catch {
      items = [];
    }

    let deliveryAddress: DeliveryAddress | null = null;
    if (doc.delivery_address) {
      try {
        deliveryAddress = JSON.parse(doc.delivery_address) as DeliveryAddress;
      } catch {
        deliveryAddress = null;
      }
    }

    return {
      id: doc.$id,
      date: doc.date,
      totalAmount: doc.amount,
      status: (doc.buy_state as OrderStatus) ?? "UNVERIFIED",
      items,
      currency: (doc.currency as Currency) ?? "CUP",
      userId: doc.user_id,
      deliveryType: (doc.delivery_type as DeliveryType) ?? null,
      deliveryAddress,
      orderType: (doc.sale_type as OrderType) ?? null,
      stockHoldApplied: doc.stock_hold_applied ?? false,
      createdAt: doc.$createdAt,
      updatedAt: doc.$updatedAt,
    };
  }
}

interface AppwriteSaleDoc {
  $id: string;
  $createdAt: string;
  $updatedAt: string;
  date: string;
  amount: number;
  buy_state: string;
  currency: string;
  products: string;
  user_id: string;
  delivery_type?: string | null;
  delivery_address?: string | null;
  sale_type?: string | null;
  stock_hold_applied?: boolean;
}

interface RawSaleItem {
  productId?: string;
  productName?: string | null;
  quantity?: number;
  unitPrice?: number | null;
  listUnitPrice?: number | null;
  price?: number | null;
}

/**
 * Domain — Order (Sale)
 *
 * An "Order" maps to the `sale` collection in Appwrite.
 * Soft-hold stock model (Core 1):
 *   - UNVERIFIED: order placed, stock reserved (reserved++)
 *   - VERIFIED:   order confirmed by operator (MCP never sets this)
 *   - DELETED:    order cancelled, soft-hold released (reserved--)
 */

export type OrderStatus = "UNVERIFIED" | "VERIFIED" | "DELETED";
export type OrderType = "NORMAL" | "DISCOUNT" | "GIFT";
export type Currency = "CUP" | "USD" | "MLC";
export type DeliveryType = "PICKUP" | "DELIVERY";

export interface OrderItem {
  productId: string;
  productName: string | null;
  quantity: number;
  /** Effective unit price (list price at order time for B2C) */
  unitPrice: number | null;
  /** List price at time of order (audit trail) */
  listUnitPrice: number | null;
  /**
   * Alias used in Appwrite JSON (`price`) — same as unitPrice.
   * Kept optional for read compatibility with web SaleItemDTO.
   */
  price?: number | null;
}

export interface DeliveryAddress {
  province: string;
  municipality: string;
  mainStreet: string;
  betweenStreets: string | null;
  phone: string;
  houseNumber: string;
  referenceName: string | null;
}

export interface Order {
  id: string;
  /** ISO date string */
  date: string;
  totalAmount: number;
  status: OrderStatus;
  items: OrderItem[];
  currency: Currency;
  userId: string;
  deliveryType: DeliveryType | null;
  deliveryAddress: DeliveryAddress | null;
  orderType: OrderType | null;
  /** Whether soft-hold has been applied to product stock */
  stockHoldApplied: boolean;
  createdAt: string;
  updatedAt: string;
  /** Present when hold partially failed but sale document exists */
  softHoldError?: string;
}

/** Input from MCP tool / agent */
export interface CreateOrderInput {
  items: Array<{
    productId: string;
    quantity: number;
  }>;
  currency: Currency;
  deliveryType: DeliveryType;
  deliveryAddress?: DeliveryAddress;
}

/** Payload already resolved by OrderService (prices, names, amount) */
export interface CreateOrderPersistInput {
  items: OrderItem[];
  totalAmount: number;
  currency: Currency;
  deliveryType: DeliveryType;
  deliveryAddress?: DeliveryAddress;
}

export function orderStatusLabel(status: OrderStatus): string {
  switch (status) {
    case "UNVERIFIED":
      return "Pendiente de confirmación";
    case "VERIFIED":
      return "Confirmado";
    case "DELETED":
      return "Cancelado";
  }
}

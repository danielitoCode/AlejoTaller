/**
 * Domain — Order (Sale)
 *
 * An "Order" maps to the `sale` collection in Appwrite.
 * AlejoTaller uses a soft-hold stock model:
 *   - UNVERIFIED: order placed, stock reserved (reserved++)
 *   - VERIFIED:   order confirmed, stock actually decremented
 *   - DELETED:    order cancelled, soft-hold released (reserved--)
 *
 * There is no separate "reservation" collection — an UNVERIFIED order
 * is the closest concept to a pending reservation.
 */

export type OrderStatus = "UNVERIFIED" | "VERIFIED" | "DELETED";
export type OrderType = "NORMAL" | "DISCOUNT" | "GIFT";
export type Currency = "CUP" | "USD" | "MLC";
export type DeliveryType = "PICKUP" | "DELIVERY";

export interface OrderItem {
  productId: string;
  productName: string | null;
  quantity: number;
  /** Effective unit price (list price, discounted, or 0 for GIFT) */
  unitPrice: number | null;
  /** List price at time of order (audit trail) */
  listUnitPrice: number | null;
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
}

/** Input for creating a new order */
export interface CreateOrderInput {
  items: Array<{
    productId: string;
    quantity: number;
  }>;
  currency: Currency;
  deliveryType: DeliveryType;
  deliveryAddress?: DeliveryAddress;
}

/** Human-readable label for order status */
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

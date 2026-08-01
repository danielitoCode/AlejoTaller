export type StockMovementType =
    | "entrada"
    | "salida_venta"
    | "ajuste"
    | "devolucion";

export interface StockMovement {
    id: string;
    productId: string;
    type: StockMovementType;
    /** Siempre positivo; el sentido lo define `type`. */
    quantity: number;
    /** existence del producto después del movimiento. */
    balanceAfter: number;
    reason: string;
    userId: string;
    saleId?: string | null;
    createdAt: string; // ISO-8601
}

export function createStockMovement(input: StockMovement): StockMovement {
    if (!input.id?.trim()) throw new Error("StockMovement id cannot be blank");
    if (!input.productId?.trim()) throw new Error("productId cannot be blank");
    if (!input.quantity || input.quantity <= 0) {
        throw new Error("quantity must be positive");
    }
    if (input.balanceAfter == null || input.balanceAfter < 0) {
        throw new Error("balanceAfter cannot be negative");
    }
    if (!input.reason?.trim()) throw new Error("reason is required");
    if (!input.userId?.trim()) throw new Error("userId is required");

    return {
        ...input,
        quantity: Math.floor(input.quantity),
        balanceAfter: Math.floor(input.balanceAfter)
    };
}

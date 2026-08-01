export enum BuyState {
    UNVERIFIED = "UNVERIFIED",
    VERIFIED = "VERIFIED",
    DELETED = "DELETED"
}

/**
 * SALE_POLICY Core 1 — tipos de venta.
 * Afectan importe; stock baja igual al confirmar (VERIFIED).
 */
export enum SaleType {
    NORMAL = "NORMAL",
    DISCOUNT = "DISCOUNT",
    GIFT = "GIFT"
}

export enum DeliveryType {
    PICKUP = "PICKUP",
    DELIVERY = "DELIVERY"
}

export enum Currency {
    CUP = "CUP",
    USD = "USD",
    MLC = "MLC"
}

import {type BuyState, Currency, DeliveryType, type SaleType} from "./enums";

export interface Sale {
    id: string
    date: string // ISO string (mejor que LocalDate en frontend)
    amount: number
    verified: BuyState
    products: SaleItem[]
    currency: Currency
    userId: string
    deliveryType?: DeliveryType | null
    deliveryAddress?: DeliveryAddress | null
    /** SALE_POLICY: null mientras UNVERIFIED; obligatorio en VERIFIED */
    saleType?: SaleType | null
    /**
     * Soft-hold aplicado: reserved++ por cada línea al crear UNVERIFIED.
     * Evita doble hold en reintentos (idempotencia).
     */
    stockHoldApplied?: boolean
}

export interface SaleItem {
    productId: string
    productName?: string | null
    quantity: number
    /** Precio unitario efectivo (lista, descuento o 0 si GIFT) */
    unitPrice?: number | null
    /** Precio de lista al momento (auditoría) */
    listUnitPrice?: number | null
    /** @deprecated prefer unitPrice; mantenido por compat con payloads antiguos */
    price?: number
}

export interface DeliveryAddress {
    province: string
    municipality: string
    mainStreet: string
    betweenStreets?: string | null
    phone: string
    houseNumber: string
    referenceName?: string | null
}

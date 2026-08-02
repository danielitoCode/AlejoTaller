import type {Models} from "appwrite";

export interface SaleDTO extends Models.Document {
    date: string
    amount: number
    buy_state: string
    currency: string
    products: string  // JSON string serialization of SaleItemDTO[]
    user_id: string
    delivery_type?: string | null
    delivery_address?: string | null
    /** SALE_POLICY: NORMAL | DISCOUNT | GIFT — suele fijarse al VERIFIED */
    sale_type?: string | null
    /** Soft-hold ya aplicado (reserved++). Idempotencia. */
    stock_hold_applied?: boolean
    $createdAt: string
    $updatedAt: string
}

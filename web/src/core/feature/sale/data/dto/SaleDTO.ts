import type {Models} from "appwrite";

export interface SaleDTO extends Models.Document {
    date: string
    amount: number
    buy_state: string
    products: string  // JSON string serialization of SaleItemDTO[]
    user_id: string
    delivery_type?: string | null
    delivery_address?: string | null
    $createdAt: string
    $updatedAt: string
}

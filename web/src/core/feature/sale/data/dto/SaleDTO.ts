import type {SaleItemDTO} from "./SaleItemDTO";
import type {Models} from "appwrite";

export interface SaleDTO extends Models.Document {
    date: string
    amount: number
    buy_state: string
    products: SaleItemDTO[]
    user_id: string
    delivery_type?: string | null
    $createdAt: string
    $updatedAt: string
}

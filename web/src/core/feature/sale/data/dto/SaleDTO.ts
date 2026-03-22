import type {SaleItemDTO} from "./SaleItemDTO";
import type {Models} from "appwrite";

export interface SaleDTO extends Models.Document {
    date: string
    amount: number
    verified: string
    products: SaleItemDTO[]
    userId: string
    deliveryType?: string | null
    $createdAt: string
    $updatedAt: string
}
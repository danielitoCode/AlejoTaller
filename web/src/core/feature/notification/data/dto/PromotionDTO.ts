import type {Models} from "appwrite";

export interface PromotionDTO extends Models.Document{
    id: string
    title: string
    message: string
    imageUrl?: string | null
    oldPrice?: number | null
    currentPrice?: number | null
    validFromEpochMillis: number
    validUntilEpochMillis: number
    $createdAt: string
    $updatedAt: string
}
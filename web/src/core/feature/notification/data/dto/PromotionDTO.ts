import type { Models } from "appwrite"

export interface PromotionDTO extends Models.Document {
    id?: string
    productId?: string | null
    title: string
    message: string
    imageUrl?: string | null
    oldPrice?: number | null
    currentPrice?: number | null
    validFromEpochMillis: number
    validUntilEpochMillis: number
    source?: string | null
    kind?: string | null
    status?: string | null
}

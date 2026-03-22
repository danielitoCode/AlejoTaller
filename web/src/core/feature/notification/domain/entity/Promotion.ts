export interface Promotion {
    id: string
    title: string
    message: string
    imageUrl?: string | null
    oldPrice?: number | null
    currentPrice?: number | null
    validFromEpochMillis: number
    validUntilEpochMillis: number
}

export function isPromotionActive(
    promotion: Promotion,
    nowEpochMillis: number
): boolean {
    return (
        nowEpochMillis >= promotion.validFromEpochMillis &&
        nowEpochMillis <= promotion.validUntilEpochMillis
    )
}
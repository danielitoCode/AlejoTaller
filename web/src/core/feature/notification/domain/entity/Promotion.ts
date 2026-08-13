export type PromotionSource = "automatic" | "manual"
export type PromotionKind = "product_discount" | "banner"
export type PromotionStatus = "draft" | "active" | "ended" | "cancelled"

/**
 * Política B — promociones (alineado a dash `.policies/notification/PROMOTION_POLICY.md`).
 * product.price = lista; effectivePrice usa product_discount activa.
 */
export interface Promotion {
    id: string
    productId?: string | null
    title: string
    message: string
    imageUrl?: string | null
    oldPrice?: number | null
    currentPrice?: number | null
    validFromEpochMillis: number
    validUntilEpochMillis: number
    source?: PromotionSource
    kind?: PromotionKind
    status?: PromotionStatus
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

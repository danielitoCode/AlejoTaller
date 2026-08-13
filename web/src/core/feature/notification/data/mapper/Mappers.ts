import type { PromotionDTO } from "../dto/PromotionDTO"
import type { Promotion, PromotionKind, PromotionStatus } from "../../domain/entity/Promotion"

function asKind(value: unknown): PromotionKind | undefined {
    if (value === "banner" || value === "product_discount") return value
    return undefined
}

function asStatus(value: unknown): PromotionStatus | undefined {
    if (value === "draft" || value === "active" || value === "ended" || value === "cancelled") {
        return value
    }
    return undefined
}

export function promotionFromDTO(dto: PromotionDTO): Promotion {
    return {
        id: dto.$id,
        productId: dto.productId ?? null,
        title: dto.title,
        message: dto.message,
        imageUrl: dto.imageUrl ?? null,
        oldPrice: dto.oldPrice ?? null,
        currentPrice: dto.currentPrice ?? null,
        validFromEpochMillis: dto.validFromEpochMillis,
        validUntilEpochMillis: dto.validUntilEpochMillis,
        source: dto.source === "manual" ? "manual" : dto.source === "automatic" ? "automatic" : undefined,
        kind: asKind(dto.kind),
        status: asStatus(dto.status),
    }
}

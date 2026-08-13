import type { PromotionDTO } from "../dto/PromotionDTO"
import type { Promotion, PromotionKind, PromotionStatus } from "../../domain/entity/Promotion"

export type PromotionWriteDTO = {
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
        source:
            dto.source === "manual"
                ? "manual"
                : dto.source === "automatic"
                  ? "automatic"
                  : undefined,
        kind: asKind(dto.kind),
        status: asStatus(dto.status),
    }
}

/** Domain → payload Appwrite (sin $id en body). */
export function promotionToDTO(promotion: Promotion): PromotionWriteDTO {
    return {
        productId: promotion.productId ?? null,
        title: promotion.title,
        message: promotion.message,
        imageUrl: promotion.imageUrl ?? null,
        oldPrice: promotion.oldPrice ?? null,
        currentPrice: promotion.currentPrice ?? null,
        validFromEpochMillis: promotion.validFromEpochMillis,
        validUntilEpochMillis: promotion.validUntilEpochMillis,
        source: promotion.source ?? "manual",
        kind: promotion.kind ?? null,
        status: promotion.status ?? "active",
    }
}

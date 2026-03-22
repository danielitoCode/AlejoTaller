import type {PromotionDTO} from "../dto/PromotionDTO";
import type {Promotion} from "../../domain/entity/Promotion";

export type PromotionWriteDTO = Pick<
    PromotionDTO,
    | "$id"
    | "title"
    | "message"
    | "imageUrl"
    | "oldPrice"
    | "currentPrice"
    | "validFromEpochMillis"
    | "validUntilEpochMillis"
>;

export function promotionFromDTO(dto: PromotionDTO): Promotion {
    return {
        id: dto.$id,
        title: dto.title,
        message: dto.message,
        imageUrl: dto.imageUrl ?? null,
        oldPrice: dto.oldPrice ?? null,
        currentPrice: dto.currentPrice ?? null,
        validFromEpochMillis: dto.validFromEpochMillis,
        validUntilEpochMillis: dto.validUntilEpochMillis,
    };
}

/**
 * Domain → DTO (create/update payload)
 * El id de dominio se serializa en $id de Appwrite.
 */
export function promotionToDTO(promotion: Promotion): PromotionWriteDTO {
    return {
        $id: promotion.id,
        title: promotion.title,
        message: promotion.message,
        imageUrl: promotion.imageUrl ?? null,
        oldPrice: promotion.oldPrice ?? null,
        currentPrice: promotion.currentPrice ?? null,
        validFromEpochMillis: promotion.validFromEpochMillis,
        validUntilEpochMillis: promotion.validUntilEpochMillis,
    };
}
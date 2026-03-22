import type {SaleDTO} from "../dto/SaleDTO";
import type {Sale, SaleItem} from "../../domain/entity/Sale";
import {type BuyState, DeliveryType} from "../../domain/entity/enums";
import type {SaleItemDTO} from "../dto/SaleItemDTO";

export type SaleWriteDTO = Pick<
    SaleDTO,
    "$id" | "date" | "amount" | "verified" | "products" | "userId" | "deliveryType"
>;

function saleItemFromDTO(item: SaleItemDTO): SaleItem {
    return {
        productId: item.productId,
        quantity: item.quantity,
        price: item.price,
    };
}

function saleItemToDTO(item: SaleItem): SaleItemDTO {
    return {
        productId: item.productId,
        quantity: item.quantity,
        price: item.price
    };
}


export function saleFromDTO(dto: SaleDTO): Sale {
    return {
        id: dto.$id,
        date: dto.date,
        amount: dto.amount,
        verified: dto.verified as BuyState,
        products: dto.products.map(saleItemFromDTO),
        userId: dto.userId,
        deliveryType: dto.deliveryType ? (dto.deliveryType as DeliveryType) : null,
    };
}

/**
 * Domain → DTO (create/update payload)
 * El id de dominio se serializa en $id de Appwrite.
 */
export function saleToDTO(sale: Sale): SaleWriteDTO {
    return {
        $id: sale.id,
        date: sale.date,
        amount: sale.amount,
        verified: sale.verified,
        products: sale.products.map(saleItemToDTO),
        userId: sale.userId,
        deliveryType: sale.deliveryType ?? null,
    };
}
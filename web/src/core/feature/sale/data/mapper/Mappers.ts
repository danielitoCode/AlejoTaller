import type {SaleDTO} from "../dto/SaleDTO";
import type {DeliveryAddress, Sale, SaleItem} from "../../domain/entity/Sale";
import {type BuyState, DeliveryType} from "../../domain/entity/enums";
import type {SaleItemDTO} from "../dto/SaleItemDTO";

export type SaleWriteDTO = Pick<
    SaleDTO,
    "$id" | "date" | "amount" | "buy_state" | "products" | "user_id" | "delivery_type" | "delivery_address"
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
    let productsArray: SaleItem[] = [];
    let deliveryAddress: DeliveryAddress | null = null;
    try {
        const parsed = JSON.parse(dto.products);
        productsArray = Array.isArray(parsed) ? parsed.map(saleItemFromDTO) : [];
    } catch (error) {
        console.warn(`Failed to parse products for sale ${dto.$id}:`, error);
        productsArray = [];
    }

    try {
        deliveryAddress = dto.delivery_address ? JSON.parse(dto.delivery_address) as DeliveryAddress : null;
    } catch (error) {
        console.warn(`Failed to parse delivery address for sale ${dto.$id}:`, error);
        deliveryAddress = null;
    }
    
    return {
        id: dto.$id,
        date: dto.date,
        amount: dto.amount,
        verified: dto.buy_state as BuyState,
        products: productsArray,
        userId: dto.user_id,
        deliveryType: dto.delivery_type ? (dto.delivery_type as DeliveryType) : null,
        deliveryAddress,
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
        buy_state: sale.verified,
        products: JSON.stringify(sale.products.map(saleItemToDTO)),
        user_id: sale.userId,
        delivery_type: sale.deliveryType ?? null,
        delivery_address: sale.deliveryAddress ? JSON.stringify(sale.deliveryAddress) : null,
    };
}

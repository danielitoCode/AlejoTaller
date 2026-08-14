import type {SaleDTO} from "../dto/SaleDTO";
import {type DeliveryAddress, type Sale, type SaleItem} from "../../domain/entity/Sale";
import { type BuyState, Currency, DeliveryType, SaleType} from "../../domain/entity/enums";
import type {SaleItemDTO} from "../dto/SaleItemDTO";

export type SaleWriteDTO = Pick<
    SaleDTO,
    "$id" | "date" | "amount" | "currency" | "buy_state" | "products" | "user_id" | "delivery_type" | "delivery_address" | "sale_type" | "stock_hold_applied"
>;

function saleItemFromDTO(item: SaleItemDTO): SaleItem {
    const unit = Number(item.price) || 0;
    return {
        productId: item.productId,
        productName: item.productName ?? null,
        quantity: item.quantity,
        price: unit,
        unitPrice: unit,
        listUnitPrice: item.listUnitPrice ?? null,
    };
}

function saleItemToDTO(item: SaleItem): SaleItemDTO {
    const unit = item.unitPrice ?? item.price ?? 0;
    return {
        productId: item.productId,
        productName: item.productName ?? null,
        quantity: item.quantity,
        price: unit,
        listUnitPrice: item.listUnitPrice ?? null,
    };
}

function parseSaleType(value: string | null | undefined): SaleType | null {
    if (!value) return null;
    if (value === SaleType.NORMAL || value === SaleType.DISCOUNT || value === SaleType.GIFT) {
        return value as SaleType;
    }
    return null;
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
        currency: stringToCurrency(dto.currency),
        verified: dto.buy_state as BuyState,
        products: productsArray,
        userId: dto.user_id,
        deliveryType: dto.delivery_type ? (dto.delivery_type as DeliveryType) : null,
        deliveryAddress,
        saleType: parseSaleType(dto.sale_type),
        stockHoldApplied: dto.stock_hold_applied === true,
    };
}

function stringToCurrency(currency: string): Currency {
    switch (currency) {
        case "CUP":
            return Currency.CUP;
        case "USD":
            return Currency.USD;
        case "MLC":
            return Currency.MLC;
        default:
            throw new Error(`Unknown currency: ${currency}`);
    }
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
        currency: sale.currency.toString(),
        buy_state: sale.verified,
        products: JSON.stringify(sale.products.map(saleItemToDTO)),
        user_id: sale.userId,
        delivery_type: sale.deliveryType ?? null,
        delivery_address: sale.deliveryAddress ? JSON.stringify(sale.deliveryAddress) : null,
        sale_type: sale.saleType ?? null,
        stock_hold_applied: sale.stockHoldApplied === true,
    };
}

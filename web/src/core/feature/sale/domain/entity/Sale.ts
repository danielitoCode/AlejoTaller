import {type BuyState, DeliveryType} from "./enums";

export interface Sale {
    id: string
    date: string // ISO string (mejor que LocalDate en frontend)
    amount: number
    verified: BuyState
    products: SaleItem[]
    userId: string
    deliveryType?: DeliveryType | null
}

export interface SaleItem {
    productId: string
    quantity: number
    price: number
}
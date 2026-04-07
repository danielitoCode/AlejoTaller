import {type BuyState, DeliveryType} from "./enums";

export interface Sale {
    id: string
    date: string // ISO string (mejor que LocalDate en frontend)
    amount: number
    verified: BuyState
    products: SaleItem[]
    userId: string
    deliveryType?: DeliveryType | null
    deliveryAddress?: DeliveryAddress | null
}

export interface SaleItem {
    productId: string
    productName?: string | null
    quantity: number
    price: number
}

export interface DeliveryAddress {
    province: string
    municipality: string
    mainStreet: string
    betweenStreets?: string | null
    phone: string
    houseNumber: string
    referenceName?: string | null
}

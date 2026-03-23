import type { Sale } from "../../domain/entity/Sale";
import { BuyState, DeliveryType } from "../../domain/entity/enums";

export const saleFixtures: Sale[] = [
    {
        id: "rv-1204",
        date: "2026-03-22",
        amount: 210.0,
        verified: BuyState.UNVERIFIED,
        userId: "user-1",
        deliveryType: DeliveryType.DELIVERY,
        products: [
            { productId: "p1", quantity: 1, price: 149.9 },
            { productId: "p3", quantity: 2, price: 24.99 }
        ]
    },
    {
        id: "rv-1207",
        date: "2026-03-20",
        amount: 89.5,
        verified: BuyState.VERIFIED,
        userId: "user-1",
        deliveryType: DeliveryType.PICKUP,
        products: [{ productId: "p2", quantity: 1, price: 89.5 }]
    }
];

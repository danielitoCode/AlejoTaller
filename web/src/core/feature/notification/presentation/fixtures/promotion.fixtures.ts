import type { Promotion } from "../../domain/entity/Promotion";

const now = Date.now();
const oneWeek = 7 * 24 * 60 * 60 * 1000;

export const promotionFixtures: Promotion[] = [
    {
        id: "promo-1",
        title: "Hasta 50% en dispositivos seleccionados",
        message: "Promocion especial para mover inventario esta semana.",
        imageUrl: null,
        oldPrice: 130,
        currentPrice: 89.5,
        validFromEpochMillis: now - oneWeek,
        validUntilEpochMillis: now + oneWeek
    },
    {
        id: "promo-2",
        title: "Liquidacion de accesorios",
        message: "Descuento por tiempo limitado en productos de mostrador.",
        imageUrl: null,
        oldPrice: 32,
        currentPrice: 24.99,
        validFromEpochMillis: now - oneWeek,
        validUntilEpochMillis: now + oneWeek
    }
];

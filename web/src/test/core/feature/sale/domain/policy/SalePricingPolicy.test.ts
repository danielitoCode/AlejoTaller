import { describe, expect, it } from "vitest"
import type { Promotion } from "../../../../../../core/feature/notification/domain/entity/Promotion"
import {
    saleAmountFromItems,
    snapshotSaleItemFromProduct,
} from "../../../../../../core/feature/sale/domain/policy/SalePricingPolicy"

const NOW = 1_700_000_000_000
const WEEK = 7 * 24 * 60 * 60 * 1000

function promo(partial: Partial<Promotion> & Pick<Promotion, "id">): Promotion {
    return {
        title: "t",
        message: "m",
        validFromEpochMillis: NOW - WEEK,
        validUntilEpochMillis: NOW + WEEK,
        status: "active",
        kind: "product_discount",
        ...partial,
    }
}

describe("SalePricingPolicy — snapshot al crear venta", () => {
    const product = { id: "p1", name: "Cable", price: 100 }

    it("congela unitPrice efectivo y listUnitPrice", () => {
        const promos = [
            promo({
                id: "d1",
                productId: "p1",
                currentPrice: 70,
            }),
        ]
        const line = snapshotSaleItemFromProduct(product, 2, promos, NOW)
        expect(line.unitPrice).toBe(70)
        expect(line.listUnitPrice).toBe(100)
        expect(line.price).toBe(70)
        expect(line.quantity).toBe(2)
        expect(line.productName).toBe("Cable")
    })

    it("sin promo usa precio de lista", () => {
        const line = snapshotSaleItemFromProduct(product, 1, [], NOW)
        expect(line.unitPrice).toBe(100)
        expect(line.listUnitPrice).toBe(100)
    })

    it("saleAmountFromItems suma unit * qty", () => {
        const items = [
            snapshotSaleItemFromProduct(
                product,
                2,
                [promo({ id: "x", productId: "p1", currentPrice: 40 })],
                NOW
            ),
            snapshotSaleItemFromProduct({ id: "p2", name: "B", price: 10 }, 3, [], NOW),
        ]
        expect(saleAmountFromItems(items)).toBe(40 * 2 + 10 * 3)
    })

    it("convertUnit se aplica a unit y lista", () => {
        const line = snapshotSaleItemFromProduct(
            product,
            1,
            [promo({ id: "c", productId: "p1", currentPrice: 50 })],
            NOW,
            (usd) => usd * 320
        )
        expect(line.unitPrice).toBe(50 * 320)
        expect(line.listUnitPrice).toBe(100 * 320)
    })

    it("el snapshot no cambia si después cambia la promo", () => {
        const promos = [promo({ id: "d", productId: "p1", currentPrice: 80 })]
        const line = snapshotSaleItemFromProduct(product, 1, promos, NOW)
        promos[0].currentPrice = 10
        expect(line.unitPrice).toBe(80)
    })
})

import { describe, expect, it } from "vitest"
import {
    promotionFromDTO,
    promotionToDTO,
} from "../../../../../../core/feature/notification/data/mapper/Mappers"
import type { PromotionDTO } from "../../../../../../core/feature/notification/data/dto/PromotionDTO"

describe("promotion mappers", () => {
    it("promotionFromDTO mapea kind/status y campos de precio", () => {
        const dto = {
            $id: "promo-1",
            productId: "prod-9",
            title: "Oferta",
            message: "10% off",
            imageUrl: null,
            oldPrice: 100,
            currentPrice: 90,
            validFromEpochMillis: 1,
            validUntilEpochMillis: 2,
            source: "manual",
            kind: "product_discount",
            status: "active",
        } as PromotionDTO

        const p = promotionFromDTO(dto)
        expect(p.id).toBe("promo-1")
        expect(p.productId).toBe("prod-9")
        expect(p.kind).toBe("product_discount")
        expect(p.status).toBe("active")
        expect(p.oldPrice).toBe(100)
        expect(p.currentPrice).toBe(90)
    })

    it("promotionFromDTO ignora kind/status desconocidos", () => {
        const dto = {
            $id: "x",
            title: "t",
            message: "m",
            validFromEpochMillis: 0,
            validUntilEpochMillis: 1,
            kind: "weird",
            status: "pending",
        } as unknown as PromotionDTO

        const p = promotionFromDTO(dto)
        expect(p.kind).toBeUndefined()
        expect(p.status).toBeUndefined()
    })

    it("promotionToDTO escribe defaults Policy B", () => {
        const write = promotionToDTO({
            id: "id",
            title: "t",
            message: "m",
            validFromEpochMillis: 10,
            validUntilEpochMillis: 20,
            productId: "p1",
            kind: "product_discount",
        })
        expect(write.source).toBe("manual")
        expect(write.status).toBe("active")
        expect(write.kind).toBe("product_discount")
        expect(write.productId).toBe("p1")
    })
})

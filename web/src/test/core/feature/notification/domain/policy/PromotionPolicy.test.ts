import { describe, expect, it } from "vitest"
import type { Promotion } from "../../../../../../core/feature/notification/domain/entity/Promotion"
import {
    discountPercent,
    effectivePrice,
    findActiveProductPromo,
    isActiveBanner,
    isActiveProductDiscount,
    isPromotionWindowActive,
    resolvePromotionKind,
} from "../../../../../../core/feature/notification/domain/policy/PromotionPolicy"

const NOW = 1_700_000_000_000
const WEEK = 7 * 24 * 60 * 60 * 1000

function promo(partial: Partial<Promotion> & Pick<Promotion, "id">): Promotion {
    return {
        title: "Promo",
        message: "msg",
        validFromEpochMillis: NOW - WEEK,
        validUntilEpochMillis: NOW + WEEK,
        status: "active",
        ...partial,
    }
}

describe("PromotionPolicy — Policy B", () => {
    describe("discountPercent", () => {
        it("calcula el % de descuento", () => {
            expect(discountPercent(100, 75)).toBeCloseTo(25)
            expect(discountPercent(200, 100)).toBeCloseTo(50)
        })

        it("retorna 0 si no hay rebaja válida", () => {
            expect(discountPercent(0, 10)).toBe(0)
            expect(discountPercent(100, 100)).toBe(0)
            expect(discountPercent(100, 120)).toBe(0)
            expect(discountPercent(Number.NaN, 50)).toBe(0)
        })
    })

    describe("resolvePromotionKind", () => {
        it("respeta kind explícito", () => {
            expect(resolvePromotionKind({ kind: "banner", productId: "p1" })).toBe("banner")
            expect(resolvePromotionKind({ kind: "product_discount", productId: null })).toBe(
                "product_discount"
            )
        })

        it("infiere banner sin productId y product_discount con productId", () => {
            expect(resolvePromotionKind({ productId: null })).toBe("banner")
            expect(resolvePromotionKind({ productId: "" })).toBe("banner")
            expect(resolvePromotionKind({ productId: "  " })).toBe("banner")
            expect(resolvePromotionKind({ productId: "prod-1" })).toBe("product_discount")
        })
    })

    describe("isPromotionWindowActive", () => {
        it("incluye extremos de la ventana", () => {
            const p = promo({
                id: "w1",
                validFromEpochMillis: NOW,
                validUntilEpochMillis: NOW + 1000,
            })
            expect(isPromotionWindowActive(p, NOW)).toBe(true)
            expect(isPromotionWindowActive(p, NOW + 1000)).toBe(true)
            expect(isPromotionWindowActive(p, NOW - 1)).toBe(false)
            expect(isPromotionWindowActive(p, NOW + 1001)).toBe(false)
        })
    })

    describe("isActiveProductDiscount", () => {
        it("activa solo product_discount en ventana y status válido", () => {
            const ok = promo({
                id: "d1",
                kind: "product_discount",
                productId: "p1",
                currentPrice: 80,
            })
            expect(isActiveProductDiscount(ok, NOW)).toBe(true)
        })

        it("rechaza banner, draft/cancelled/ended y sin productId", () => {
            expect(
                isActiveProductDiscount(
                    promo({ id: "b1", kind: "banner", productId: null }),
                    NOW
                )
            ).toBe(false)
            expect(
                isActiveProductDiscount(
                    promo({
                        id: "d2",
                        kind: "product_discount",
                        productId: "p1",
                        status: "draft",
                    }),
                    NOW
                )
            ).toBe(false)
            expect(
                isActiveProductDiscount(
                    promo({
                        id: "d3",
                        kind: "product_discount",
                        productId: "p1",
                        status: "cancelled",
                    }),
                    NOW
                )
            ).toBe(false)
            expect(
                isActiveProductDiscount(
                    promo({
                        id: "d4",
                        kind: "product_discount",
                        productId: "",
                    }),
                    NOW
                )
            ).toBe(false)
        })

        it("fuera de ventana no está activa", () => {
            const p = promo({
                id: "d5",
                kind: "product_discount",
                productId: "p1",
                validFromEpochMillis: NOW + WEEK,
                validUntilEpochMillis: NOW + 2 * WEEK,
            })
            expect(isActiveProductDiscount(p, NOW)).toBe(false)
        })
    })

    describe("isActiveBanner", () => {
        it("activa banner en ventana", () => {
            expect(
                isActiveBanner(promo({ id: "ban1", kind: "banner", productId: null }), NOW)
            ).toBe(true)
        })

        it("no trata product_discount como banner", () => {
            expect(
                isActiveBanner(
                    promo({ id: "ban2", kind: "product_discount", productId: "p1" }),
                    NOW
                )
            ).toBe(false)
        })
    })

    describe("effectivePrice (lista estable + promo activa)", () => {
        const list = 100
        const promos: Promotion[] = [
            promo({
                id: "a",
                kind: "product_discount",
                productId: "p1",
                currentPrice: 70,
                validFromEpochMillis: NOW - 1000,
            }),
            promo({
                id: "b",
                kind: "product_discount",
                productId: "p1",
                currentPrice: 60,
                validFromEpochMillis: NOW - 500,
            }),
            promo({
                id: "c",
                kind: "product_discount",
                productId: "p2",
                currentPrice: 50,
            }),
            promo({
                id: "d",
                kind: "banner",
                productId: null,
                currentPrice: 10,
            }),
        ]

        it("sin promo del producto devuelve precio de lista", () => {
            expect(effectivePrice(list, "p3", promos, NOW)).toBe(100)
            expect(effectivePrice(list, "", promos, NOW)).toBe(100)
        })

        it("elige el menor currentPrice entre activas del mismo producto", () => {
            expect(effectivePrice(list, "p1", promos, NOW)).toBe(60)
        })

        it("no aplica banner ni promo de otro producto", () => {
            expect(effectivePrice(list, "p2", promos, NOW)).toBe(50)
            expect(effectivePrice(list, "p1", [promos[3]], NOW)).toBe(100)
        })

        it("currentPrice inválido cae a lista", () => {
            const bad = promo({
                id: "bad",
                kind: "product_discount",
                productId: "p9",
                currentPrice: Number.NaN,
            })
            expect(effectivePrice(90, "p9", [bad], NOW)).toBe(90)
        })
    })

    describe("findActiveProductPromo", () => {
        it("devuelve la más reciente por validFrom", () => {
            const older = promo({
                id: "old",
                kind: "product_discount",
                productId: "p1",
                currentPrice: 80,
                validFromEpochMillis: NOW - 2000,
            })
            const newer = promo({
                id: "new",
                kind: "product_discount",
                productId: "p1",
                currentPrice: 70,
                validFromEpochMillis: NOW - 100,
            })
            const found = findActiveProductPromo("p1", [older, newer], NOW)
            expect(found?.id).toBe("new")
        })

        it("null si no hay activa", () => {
            expect(findActiveProductPromo("px", [], NOW)).toBeNull()
        })
    })
})

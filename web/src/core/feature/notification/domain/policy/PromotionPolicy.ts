import type { Promotion, PromotionKind } from "../entity/Promotion"

export function discountPercent(oldPrice: number, promoPrice: number): number {
    const oldP = Number(oldPrice)
    const promo = Number(promoPrice)
    if (!Number.isFinite(oldP) || oldP <= 0) return 0
    if (!Number.isFinite(promo) || promo < 0) return 0
    if (promo >= oldP) return 0
    return ((oldP - promo) / oldP) * 100
}

export function resolvePromotionKind(
    promo: Pick<Promotion, "productId"> & { kind?: string }
): PromotionKind {
    if (promo.kind === "banner" || promo.kind === "product_discount") return promo.kind
    const pid = promo.productId
    if (pid === undefined || pid === null || String(pid).trim() === "") return "banner"
    return "product_discount"
}

export function isPromotionWindowActive(
    promo: Pick<Promotion, "validFromEpochMillis" | "validUntilEpochMillis">,
    nowEpochMillis: number
): boolean {
    const now = Number(nowEpochMillis)
    return now >= Number(promo.validFromEpochMillis) && now <= Number(promo.validUntilEpochMillis)
}

export function isActiveProductDiscount(promo: Promotion, nowEpochMillis: number): boolean {
    if (resolvePromotionKind(promo) !== "product_discount") return false
    const status = promo.status
    if (status === "cancelled" || status === "ended" || status === "draft") return false
    if (!promo.productId || String(promo.productId).trim() === "") return false
    return isPromotionWindowActive(promo, nowEpochMillis)
}

export function isActiveBanner(promo: Promotion, nowEpochMillis: number): boolean {
    if (resolvePromotionKind(promo) !== "banner") return false
    const status = promo.status
    if (status === "cancelled" || status === "ended" || status === "draft") return false
    return isPromotionWindowActive(promo, nowEpochMillis)
}

export function effectivePrice(
    listPrice: number,
    productId: string,
    promos: Promotion[],
    nowEpochMillis: number = Date.now()
): number {
    const list = Number(listPrice)
    const base = Number.isFinite(list) && list >= 0 ? list : 0
    const pid = String(productId || "").trim()
    if (!pid) return base

    const active = promos.filter(
        (p) => isActiveProductDiscount(p, nowEpochMillis) && String(p.productId).trim() === pid
    )
    if (active.length === 0) return base

    active.sort((a, b) => {
        const pa = Number(a.currentPrice)
        const pb = Number(b.currentPrice)
        if (Number.isFinite(pa) && Number.isFinite(pb) && pa !== pb) return pa - pb
        return Number(b.validFromEpochMillis) - Number(a.validFromEpochMillis)
    })
    const chosen = Number(active[0].currentPrice)
    if (!Number.isFinite(chosen) || chosen < 0) return base
    return chosen
}

export function findActiveProductPromo(
    productId: string,
    promos: Promotion[],
    nowEpochMillis: number = Date.now()
): Promotion | null {
    const pid = String(productId || "").trim()
    if (!pid) return null
    const active = promos.filter(
        (p) => isActiveProductDiscount(p, nowEpochMillis) && String(p.productId).trim() === pid
    )
    if (active.length === 0) return null
    active.sort((a, b) => Number(b.validFromEpochMillis) - Number(a.validFromEpochMillis))
    return active[0]
}

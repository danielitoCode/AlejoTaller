import type { Promotion } from "../../domain/entity/Promotion"
import { getPrimaryProductImageUrl } from "../../../product/presentation/utils/product.images"
import { resolvePromotionKind } from "../../domain/policy/PromotionPolicy"

/**
 * Imagen a mostrar en UI de promo:
 * 1) imageUrl de la promo si es http(s)
 * 2) si es product_discount sin imagen → foto primaria del producto
 * Nota: hotlinks de Pinterest u otros CDN suelen fallar por referrer; preferir Appwrite storage.
 */
export function resolvePromotionImageUrl(
    promo: Promotion,
    productPhotoById: Map<string, string | null | undefined> | Record<string, string | null | undefined> = {}
): string | null {
    const raw = promo.imageUrl
    if (typeof raw === "string") {
        const trimmed = raw.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
    }

    const kind = resolvePromotionKind(promo)
    if (kind !== "product_discount") return null

    const pid = String(promo.productId ?? "").trim()
    if (!pid) return null

    const photo =
        productPhotoById instanceof Map
            ? productPhotoById.get(pid)
            : productPhotoById[pid]

    return getPrimaryProductImageUrl(photo) ?? null
}

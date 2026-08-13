package com.elitec.shared.core.feature.notifications.domain.entity

/**
 * Política B — precio de lista en product.price; effectivePrice vía promo activa.
 */
data class Promotion(
    val id: String,
    val productId: String? = null,
    val title: String,
    val message: String,
    val imageUrl: String?,
    val oldPrice: Double? = null,
    val currentPrice: Double? = null,
    val validFromEpochMillis: Long,
    val validUntilEpochMillis: Long,
    val kind: String? = null,
    val status: String? = null,
    val source: String? = null,
) {
    fun isWindowActive(nowEpochMillis: Long): Boolean =
        nowEpochMillis in validFromEpochMillis..validUntilEpochMillis

    fun isActive(nowEpochMillis: Long): Boolean =
        isWindowActive(nowEpochMillis) &&
            status != "cancelled" &&
            status != "ended" &&
            status != "draft"

    fun resolvedKind(): String {
        if (kind == "banner" || kind == "product_discount") return kind
        return if (productId.isNullOrBlank()) "banner" else "product_discount"
    }

    fun isActiveProductDiscount(nowEpochMillis: Long): Boolean =
        resolvedKind() == "product_discount" &&
            !productId.isNullOrBlank() &&
            isActive(nowEpochMillis)

    fun isActiveBanner(nowEpochMillis: Long): Boolean =
        resolvedKind() == "banner" && isActive(nowEpochMillis)
}

object PromotionPricing {
    fun discountPercent(oldPrice: Double, promoPrice: Double): Double {
        if (oldPrice <= 0.0 || promoPrice < 0.0 || promoPrice >= oldPrice) return 0.0
        return ((oldPrice - promoPrice) / oldPrice) * 100.0
    }

    fun effectivePrice(
        listPrice: Double,
        productId: String,
        promos: List<Promotion>,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ): Double {
        val base = if (listPrice.isFinite() && listPrice >= 0.0) listPrice else 0.0
        val pid = productId.trim()
        if (pid.isEmpty()) return base

        val active = promos.filter {
            it.isActiveProductDiscount(nowEpochMillis) && it.productId?.trim() == pid
        }
        if (active.isEmpty()) return base

        val chosen = active
            .sortedWith(
                compareBy<Promotion> { it.currentPrice ?: Double.MAX_VALUE }
                    .thenByDescending { it.validFromEpochMillis },
            )
            .first()
        val price = chosen.currentPrice
        return if (price != null && price.isFinite() && price >= 0.0) price else base
    }
}

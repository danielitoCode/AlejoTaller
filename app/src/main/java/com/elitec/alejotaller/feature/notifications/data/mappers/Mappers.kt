package com.elitec.alejotaller.feature.notifications.data.mappers

import com.elitec.alejotaller.feature.notifications.data.dto.PromotionDto
import com.elitec.alejotaller.feature.notifications.data.models.PromotionEvent
import com.elitec.shared.core.feature.notifications.domain.entity.Promotion

fun PromotionDto.toDomain(): Promotion = Promotion(
    id = id,
    productId = productId,
    title = title,
    message = message,
    imageUrl = imageUrl,
    oldPrice = oldPrice,
    currentPrice = currentPrice,
    validFromEpochMillis = validFromEpochMillis,
    validUntilEpochMillis = validUntilEpochMillis,
    kind = kind,
    status = status,
    source = source,
)

fun Promotion.toDto(): PromotionDto = PromotionDto(
    id = id,
    productId = productId,
    title = title,
    message = message,
    imageUrl = imageUrl,
    oldPrice = oldPrice,
    currentPrice = currentPrice,
    validFromEpochMillis = validFromEpochMillis,
    validUntilEpochMillis = validUntilEpochMillis,
    kind = kind,
    status = status,
    source = source,
)

private const val DEFAULT_PROMO_TTL = 1000L * 60L * 60L * 24L * 7L

fun PromotionEvent.toDomainPromotion(): Promotion {
    val now = System.currentTimeMillis()

    return Promotion(
        id = id.ifBlank { "promo-$now" },
        productId = null,
        title = title,
        message = message,
        imageUrl = imageUrl,
        oldPrice = oldPrice,
        currentPrice = currentPrice,
        validFromEpochMillis = validFromEpochMillis ?: now,
        validUntilEpochMillis = validUntilEpochMillis ?: (now + DEFAULT_PROMO_TTL),
    )
}

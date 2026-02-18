package com.elitec.alejotaller.feature.notifications.data.mappers

import com.elitec.alejotaller.feature.notifications.data.dto.PromotionDto
import com.elitec.alejotaller.feature.notifications.data.models.PromotionEvent
import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion

fun PromotionDto.toDomain(): Promotion = Promotion(
    id = id,
    title = title,
    message = message,
    imageUrl = imageUrl,
    oldPrice = oldPrice,
    currentPrice = currentPrice,
    validFromEpochMillis = validFromEpochMillis,
    validUntilEpochMillis = validUntilEpochMillis
)

fun Promotion.toDto(): PromotionDto = PromotionDto(
    id = id,
    title = title,
    message = message,
    imageUrl = imageUrl,
    oldPrice = oldPrice,
    currentPrice = currentPrice,
    validFromEpochMillis = validFromEpochMillis,
    validUntilEpochMillis = validUntilEpochMillis
)

private const val DEFAULT_PROMO_TTL = 1000L * 60L * 60L * 24L * 7L

fun PromotionEvent.toDomainPromotion(): Promotion {
    val now = System.currentTimeMillis()

    return Promotion(
        id = id.ifBlank { "promo-$now" },
        title = title,
        message = message,
        imageUrl = imageUrl,
        oldPrice = oldPrice,
        currentPrice = currentPrice,
        validFromEpochMillis = validFromEpochMillis ?: now,
        validUntilEpochMillis = validUntilEpochMillis ?: (now + DEFAULT_PROMO_TTL)
    )
}
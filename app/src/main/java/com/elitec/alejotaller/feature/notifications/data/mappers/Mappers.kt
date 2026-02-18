package com.elitec.alejotaller.feature.notifications.data.mappers

import com.elitec.alejotaller.feature.notifications.data.dto.PromotionDto
import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion

fun PromotionDto.toDomain(): Promotion = Promotion(
    id = id,
    title = title,
    message = message,
    imageUrl = imageUrl,
    validFromEpochMillis = validFromEpochMillis,
    validUntilEpochMillis = validUntilEpochMillis
)

fun Promotion.toDto(): PromotionDto = PromotionDto(
    id = id,
    title = title,
    message = message,
    imageUrl = imageUrl,
    validFromEpochMillis = validFromEpochMillis,
    validUntilEpochMillis = validUntilEpochMillis
)
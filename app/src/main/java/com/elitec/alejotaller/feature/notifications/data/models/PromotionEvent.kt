package com.elitec.alejotaller.feature.notifications.data.models

data class PromotionEvent(
    val id: String,
    val title: String,
    val message: String,
    val imageUrl: String? = null,
    val oldPrice: Double? = null,
    val currentPrice: Double? = null,
    val validFromEpochMillis: Long? = null,
    val validUntilEpochMillis: Long? = null
)
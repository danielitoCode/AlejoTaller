package com.elitec.alejotaller.feature.notifications.domain.entity

data class Promotion(
    val id: String,
    val title: String,
    val message: String,
    val imageUrl: String?,
    val oldPrice: Double? = null,
    val currentPrice: Double? = null,
    val validFromEpochMillis: Long,
    val validUntilEpochMillis: Long
) {
    fun isActive(nowEpochMillis: Long): Boolean =
        nowEpochMillis in validFromEpochMillis..validUntilEpochMillis
}
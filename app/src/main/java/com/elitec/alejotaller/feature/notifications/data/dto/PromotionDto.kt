package com.elitec.alejotaller.feature.notifications.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promotions")
data class PromotionDto(
    @PrimaryKey val id: String,
    val productId: String? = null,
    val title: String,
    val message: String,
    val imageUrl: String?,
    val oldPrice: Double?,
    val currentPrice: Double?,
    val validFromEpochMillis: Long,
    val validUntilEpochMillis: Long,
    val kind: String? = null,
    val status: String? = null,
    val source: String? = null,
)

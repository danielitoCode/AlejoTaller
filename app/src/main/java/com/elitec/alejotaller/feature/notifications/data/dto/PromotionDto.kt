package com.elitec.alejotaller.feature.notifications.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "promotions")
data class PromotionDto(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val imageUrl: String?,
    val validFromEpochMillis: Long,
    val validUntilEpochMillis: Long
)
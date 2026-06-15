package com.elitec.alejotaller.feature.exchange.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class CupExchangeLocalDto(
    @PrimaryKey val id: String,
    val usdReference: Float,
    val euroReference: Float,
    val updatedAt: String,
    val source: String
)

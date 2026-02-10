package com.elitec.alejotaller.feature.sale.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SaleDto(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val amount: Double,
    val products: List<String>,
    @SerialName("user_id")
    val userId: String
)
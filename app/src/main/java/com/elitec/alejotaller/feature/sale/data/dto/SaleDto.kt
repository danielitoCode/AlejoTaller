package com.elitec.alejotaller.feature.sale.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
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
    val verified: String,
    val products: List<SaleItem>,
    @SerialName("user_id")
    val userId: String,
    @SerialName("delivery_type")
    val deliveryType: String? = null
)

fun String.toBuyState(): BuyState = when(this) {
    "UNVERIFIED" -> BuyState.UNVERIFIED
    "VERIFIED" -> BuyState.VERIFIED
    "DELETED" -> BuyState.DELETED
    else -> throw Exception("Estado de venta imposible de resolver")
}

fun String.toDeliveryType(): DeliveryType = when(this) {
    "PICKUP" -> DeliveryType.PICKUP
    "DELIVERY" -> DeliveryType.DELIVERY
    else -> throw Exception("Estado de venta imposible de resolver")
}

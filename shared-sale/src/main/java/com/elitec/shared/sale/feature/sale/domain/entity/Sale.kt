package com.elitec.shared.sale.feature.sale.domain.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: String,
    val date: LocalDate,
    val amount: Double,
    val verified: BuyState,
    val products: List<SaleItem>,
    val userId: String,
    val deliveryType: DeliveryType? = null,
    val deliveryAddress: DeliveryAddress? = null
)

enum class BuyState {
    UNVERIFIED, VERIFIED, DELETED
}

/**
 * Preferencia de entrega que el cliente elige cuando su pedido
 * está en estado VERIFIED (listo para entrega).
 */
enum class DeliveryType {
    PICKUP,   // El cliente va a recogerlo al taller
    DELIVERY  // El cliente solicita domicilio (el taller coordina)
}

@Serializable
data class DeliveryAddress(
    val province: String,
    val municipality: String,
    val mainStreet: String,
    val betweenStreets: String? = null,
    val phone: String,
    val houseNumber: String,
    val referenceName: String? = null
)


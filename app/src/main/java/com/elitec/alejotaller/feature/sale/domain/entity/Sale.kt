package com.elitec.alejotaller.feature.sale.domain.entity

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class Sale(
    val id: String,
    val date: LocalDate,
    val amount: Double,
    val verified: BuyState,
    val products: List<SaleItem>,
    val userId: String,
    val deliveryType: DeliveryType? = null
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


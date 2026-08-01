package com.elitec.shared.sale.feature.sale.domain.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: String,
    val date: LocalDate,
    val amount: Double,
    val currency: Currency,
    val verified: BuyState,
    val products: List<SaleItem>,
    val userId: String,
    val customerName: String? = null,
    val deliveryType: DeliveryType? = null,
    val deliveryAddress: DeliveryAddress? = null,
    /**
     * SALE_POLICY: tipo comercial al cerrar en operador.
     * null mientras UNVERIFIED (provisional); obligatorio al pasar a VERIFIED.
     */
    val saleType: SaleType? = null
)

enum class BuyState {
    UNVERIFIED, VERIFIED, DELETED
}

/**
 * SALE_POLICY Core 1 — tipos de venta definidos en tienda.
 * Afectan importe; no eximen de baja de stock al confirmar.
 */
enum class SaleType {
    /** Precio de lista del catálogo. */
    NORMAL,
    /** Descuento alineado con dueño o promoción. */
    DISCOUNT,
    /** Regalia / obsequio — amount 0, stock sí baja. */
    GIFT
}

/**
 * Preferencia de entrega que el cliente elige cuando su pedido
 * está en estado VERIFIED (listo para entrega).
 */
enum class DeliveryType {
    PICKUP,   // El cliente va a recogerlo al taller
    DELIVERY  // El cliente solicita domicilio (el taller coordina)
}

enum class Currency { CUP, USD, MLC }

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

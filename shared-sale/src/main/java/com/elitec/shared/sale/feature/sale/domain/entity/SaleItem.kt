package com.elitec.shared.sale.feature.sale.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SaleItem(
    val productId: String,
    val quantity: Int,
    val productName: String? = null,
    /**
     * Precio unitario efectivo al cerrar la venta (lista, descontado o 0 si GIFT).
     * null en solicitudes antiguas / UNVERIFIED sin precio congelado.
     */
    val unitPrice: Double? = null,
    /** Precio de lista al momento (auditoría de descuento). */
    val listUnitPrice: Double? = null
) {
    init {
        require(productId.isNotBlank()) { "Product id cannot be blank" }
        require(quantity > 0) { "Quantity must be greater than 0" }
        if (unitPrice != null) {
            require(unitPrice >= 0.0) { "unitPrice cannot be negative" }
        }
        if (listUnitPrice != null) {
            require(listUnitPrice >= 0.0) { "listUnitPrice cannot be negative" }
        }
    }

    /** Importe de línea usando precio efectivo; 0 si no hay unitPrice. */
    fun lineAmount(): Double = (unitPrice ?: 0.0) * quantity
}

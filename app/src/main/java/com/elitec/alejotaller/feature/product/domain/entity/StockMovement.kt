package com.elitec.alejotaller.feature.product.domain.entity

/**
 * Movimiento de inventario (colección Appwrite `stock_movements`).
 * quantity siempre > 0; el sentido lo define [type].
 */
enum class StockMovementType {
    ENTRADA,
    SALIDA_VENTA,
    AJUSTE,
    DEVOLUCION
}

data class StockMovement(
    val id: String,
    val productId: String,
    val type: StockMovementType,
    val quantity: Int,
    val balanceAfter: Int,
    val reason: String,
    val userId: String,
    val saleId: String? = null,
    /** ISO-8601 o epoch millis según capa de persistencia. */
    val createdAtIso: String
) {
    init {
        require(id.isNotBlank()) { "StockMovement id cannot be blank" }
        require(productId.isNotBlank()) { "productId cannot be blank" }
        require(quantity > 0) { "quantity must be positive" }
        require(balanceAfter >= 0) { "balanceAfter cannot be negative" }
        require(reason.isNotBlank()) { "reason is required" }
        require(userId.isNotBlank()) { "userId is required" }
    }
}

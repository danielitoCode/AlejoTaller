package com.elitec.alejotallerscan.feature.inventory.domain.entity

/**
 * Escritura de stock_movements desde operador (Core 2 B2).
 * type fijo salida_venta en confirmación VERIFIED.
 */
data class StockMovementWrite(
    val productId: String,
    val type: String,
    val quantity: Int,
    val balanceAfter: Int,
    val reason: String,
    val userId: String,
    val saleId: String?
)

data class StockMovementRecord(
    val id: String,
    val productId: String,
    val type: String,
    val quantity: Int,
    val balanceAfter: Int,
    val saleId: String?
)

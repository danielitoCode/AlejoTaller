package com.elitec.alejotaller.feature.product.domain.realtime

/**
 * Contrato alineado con web: canal stock-updates, evento stock:changed.
 */
data class StockChangedPayload(
    val productIds: List<String>,
    val reason: String, // hold | release | consume
    val saleId: String? = null,
    val timestamp: String
)

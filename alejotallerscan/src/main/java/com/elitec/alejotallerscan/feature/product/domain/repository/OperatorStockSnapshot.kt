package com.elitec.alejotallerscan.feature.product.domain.repository

/**
 * Resultado de aplicar deltas de stock en product.
 * lastUnitCost se lee del documento para COGS al VERIFIED (Core 2).
 */
data class OperatorStockSnapshot(
    val existenceAfter: Int,
    val reservedAfter: Int,
    val lastUnitCost: Double?
)

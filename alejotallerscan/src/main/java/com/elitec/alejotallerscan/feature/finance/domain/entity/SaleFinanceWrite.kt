package com.elitec.alejotallerscan.feature.finance.domain.entity

/**
 * Evento financiero al VERIFIED. UNVERIFIED / DELETED no generan documento.
 * COGS = sum(last_unit_cost × qty). margin = revenue − cogs.
 */
data class SaleFinanceWrite(
    val saleId: String,
    val revenue: Double,
    val cogs: Double,
    val margin: Double,
    val userId: String,
    val atIso: String,
    val currency: String?
)

data class SaleFinanceRecord(
    val id: String,
    val saleId: String,
    val revenue: Double,
    val cogs: Double,
    val margin: Double
)

package com.elitec.alejotallerscan.feature.finance.domain.entity

/**
 * Línea de sale_finance_event (Core 4).
 * unitCostSnapshot = last_unit_cost congelado al confirm.
 * Paridad con dash SaleFinanceLine / lines_json.
 */
data class SaleFinanceLineWrite(
    val productId: String,
    val quantity: Int,
    val unitPrice: Double,
    val unitCostSnapshot: Double,
    val lineRevenue: Double,
    val lineCogs: Double,
    val lineMargin: Double
)

/**
 * Evento financiero al VERIFIED. UNVERIFIED / DELETED no generan documento.
 * COGS = sum(unitCostSnapshot × qty). margin = revenue − cogs.
 * Core 4: [lines] se serializa a lines_json en Appwrite (Opción A).
 */
data class SaleFinanceWrite(
    val saleId: String,
    val revenue: Double,
    val cogs: Double,
    val margin: Double,
    val userId: String,
    val atIso: String,
    val currency: String?,
    val lines: List<SaleFinanceLineWrite> = emptyList()
)

data class SaleFinanceRecord(
    val id: String,
    val saleId: String,
    val revenue: Double,
    val cogs: Double,
    val margin: Double,
    val lines: List<SaleFinanceLineWrite> = emptyList()
)

package com.elitec.alejotallerscan.feature.finance.domain.repository

import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceRecord
import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceWrite

interface OperatorSaleFinanceRepository {
    suspend fun getBySaleId(saleId: String): SaleFinanceRecord?

    /** Crea si no existe por sale_id; si existe, devuelve el existente. */
    suspend fun createIdempotent(event: SaleFinanceWrite): SaleFinanceRecord
}

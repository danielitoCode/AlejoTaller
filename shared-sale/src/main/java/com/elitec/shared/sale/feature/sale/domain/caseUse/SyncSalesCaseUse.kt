package com.elitec.shared.sale.feature.sale.domain.caseUse

import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository

class SyncSalesCaseUse (
    private val repository: SaleRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> = repository.sync(userId)
}

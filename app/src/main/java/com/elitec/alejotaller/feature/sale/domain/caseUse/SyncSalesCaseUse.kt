package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository

class SyncSalesCaseUse (
    private val repository: SaleRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> = repository.sync(userId)
}
package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository

class GetSalesByIdCaseUse(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(id: String): Result<Sale> = runCatching {
        repository.getById(id)
    }
}
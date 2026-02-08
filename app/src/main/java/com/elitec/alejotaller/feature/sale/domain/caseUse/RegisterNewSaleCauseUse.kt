package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository

class RegisterNewSaleCauseUse(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(sale: Sale): Result<Unit> =  runCatching {
        repository.save(sale)
    }
}
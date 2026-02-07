package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.infraestructure.core.domain.Repository

class RegisterNewSaleCauseUse(
    private val repository: Repository<Sale>
) {
    suspend operator fun invoke(sale: Sale): Result<Unit> =  runCatching {
        repository.save(sale)
    }
}
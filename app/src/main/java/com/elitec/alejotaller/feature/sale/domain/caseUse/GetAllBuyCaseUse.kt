package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.infraestructure.core.domain.Repository

class GetAllBuyCaseUse(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Sale>> = runCatching {
        repository.getAll(userId)
    }
}
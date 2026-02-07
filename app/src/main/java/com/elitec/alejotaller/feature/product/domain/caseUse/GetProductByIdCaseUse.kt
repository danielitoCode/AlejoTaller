package com.elitec.alejotaller.feature.product.domain.caseUse

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.infraestructure.core.domain.Repository

class GetProductByIdCaseUse(
    private val repository: Repository<Product>
) {
    // Cuando se le agregue offline first se debe devolver el flow
    suspend operator fun invoke(id: String): Result<Product> = runCatching {
        repository.getById(id)
    }
}
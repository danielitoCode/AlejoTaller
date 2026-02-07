package com.elitec.alejotaller.feature.product.domain.caseUse

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository

class GetProductByIdCaseUse(
    private val repository: ProductRepository
) {
    // Cuando se le agregue offline first se debe devolver el flow
    suspend operator fun invoke(id: String): Result<Product> = runCatching {
        repository.getById(id)
    }
}
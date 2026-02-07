package com.elitec.alejotaller.feature.product.domain.caseUse

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.infraestructure.core.domain.Repository

class SyncProductCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): Result<List<Product>> = runCatching {
        repository.getAll()
    }
}
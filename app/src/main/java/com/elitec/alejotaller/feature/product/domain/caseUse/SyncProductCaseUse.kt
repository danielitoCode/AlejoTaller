package com.elitec.alejotaller.feature.product.domain.caseUse

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.infraestructure.core.domain.Repository

class SyncProductCaseUse(
    private val repository: Repository<Product>
) {
    suspend operator fun invoke(): Result<List<Product>> = runCatching {
        repository.getAll()
    }
}
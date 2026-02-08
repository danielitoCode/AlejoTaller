package com.elitec.alejotaller.feature.product.domain.caseUse

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository

class SyncProductCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.sync()
}
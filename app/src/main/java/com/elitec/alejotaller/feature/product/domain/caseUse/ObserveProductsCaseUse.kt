package com.elitec.alejotaller.feature.product.domain.caseUse

import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import com.elitec.alejotaller.feature.product.data.repository.ProductOfflineFirstRepository
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class ObserveProductsCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(): Flow<List<Product>> = repository.observeAll()
}
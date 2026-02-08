package com.elitec.alejotaller.feature.product.data.repository

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository

class ProductNetRepositoryImpl: ProductRepository {
    override suspend fun getAll(): List<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(itemId: String): Product {
        TODO("Not yet implemented")
    }
}
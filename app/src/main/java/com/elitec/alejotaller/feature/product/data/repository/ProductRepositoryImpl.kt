package com.elitec.alejotaller.feature.product.data.repository

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.infraestructure.core.domain.Repository
import io.ktor.client.HttpClient

class ProductRepositoryImpl: ProductRepository {
    override suspend fun getAll(): List<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(itemId: String): Product {
        TODO("Not yet implemented")
    }

    override suspend fun save(
        item: Product,
        onSave: () -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(
        item: Product,
        onDelete: () -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun modify(
        itemId: String,
        item: Product,
        onModify: () -> Unit,
    ) {
        TODO("Not yet implemented")
    }
}
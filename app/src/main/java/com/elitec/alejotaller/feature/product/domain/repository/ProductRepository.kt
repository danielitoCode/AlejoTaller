package com.elitec.alejotaller.feature.product.domain.repository

import com.elitec.alejotaller.feature.product.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun observeAll(): Flow<List<Product>>
    suspend fun getById(itemId: String): Product
    suspend fun sync(): Result<Unit>
}
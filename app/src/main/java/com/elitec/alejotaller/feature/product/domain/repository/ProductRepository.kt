package com.elitec.alejotaller.feature.product.domain.repository

import com.elitec.alejotaller.feature.product.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    suspend fun getById(itemId: String): Product?
    suspend fun sync(): Result<Unit>

    /**
     * Soft-hold: incrementa `reserved` en remoto + cache local.
     * @return producto actualizado o null si no existe.
     */
    suspend fun incrementReserved(productId: String, quantity: Int): Product?
}

package com.elitec.alejotaller.feature.product.domain.repository

import com.elitec.alejotaller.feature.product.domain.entity.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    suspend fun getById(itemId: String): Product?
    suspend fun sync(): Result<Unit>

    /** Soft-hold: incrementa reserved en remoto + cache local. */
    suspend fun incrementReserved(productId: String, quantity: Int): Product?

    /** Rollback soft-hold: decrementa reserved (clamp >= 0). */
    suspend fun decrementReserved(productId: String, quantity: Int): Product?

    /**
     * Fuente de verdad: lee Appwrite por id y actualiza cache offline-first.
     * Usado por soft-hold concurrente y por stock:changed.
     */
    suspend fun refreshFromRemote(productId: String): Product?
}

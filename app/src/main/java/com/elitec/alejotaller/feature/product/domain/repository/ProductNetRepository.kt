package com.elitec.alejotaller.feature.product.domain.repository

import com.elitec.alejotaller.feature.product.data.dto.ProductDto

interface ProductNetRepository {
    suspend fun getAll(): List<ProductDto>
    suspend fun getById(itemId: String): ProductDto

    /**
     * Core 1: modifica reserved directamente en Appwrite.
     * La operación es atómica y maxReserved limita el valor final.
     */
    suspend fun incrementReserved(
        productId: String,
        quantity: Int,
        maxReserved: Int
    ): ProductDto

    /** Core 1: libera reserved directamente en Appwrite con mínimo 0. */
    suspend fun decrementReserved(productId: String, quantity: Int): ProductDto
}

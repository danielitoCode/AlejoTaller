package com.elitec.alejotaller.feature.product.domain.repository

import com.elitec.alejotaller.feature.product.data.dto.ProductDto

interface ProductNetRepository {
    suspend fun getAll(): List<ProductDto>
    suspend fun getById(itemId: String): ProductDto
    /** Actualiza solo reserved (soft-hold) en Appwrite. */
    suspend fun updateReserved(productId: String, reserved: Int): ProductDto
}

package com.elitec.alejotaller.feature.product.domain.repository

import com.elitec.alejotaller.feature.product.data.dto.ProductDto

interface ProductNetRepository {
    suspend fun getById(itemId: String): ProductDto

    suspend fun getAll(): List<ProductDto>
}
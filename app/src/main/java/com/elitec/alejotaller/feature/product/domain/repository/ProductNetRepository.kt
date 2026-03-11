package com.elitec.alejotaller.feature.product.domain.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.data.mapper.toProductDto

interface ProductNetRepository {
    suspend fun getById(itemId: String): ProductDto

    suspend fun getAll(): List<ProductDto>
}
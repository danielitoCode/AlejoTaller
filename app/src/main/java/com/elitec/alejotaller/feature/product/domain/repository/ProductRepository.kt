package com.elitec.alejotaller.feature.product.domain.repository

import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.product.domain.entity.Product

interface ProductRepository {
    suspend fun getAll(): List<Product>
    suspend fun getById(itemId: String): Product
}
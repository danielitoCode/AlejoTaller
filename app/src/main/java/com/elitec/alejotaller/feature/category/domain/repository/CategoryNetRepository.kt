package com.elitec.alejotaller.feature.category.domain.repository

import com.elitec.alejotaller.feature.category.data.dto.CategoryDto

interface CategoryNetRepository {
    suspend fun getAll(): List<CategoryDto>
}
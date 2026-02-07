package com.elitec.alejotaller.feature.category.data.repository

import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository

class CategoriesRepositoryImpl: CategoriesRepository {
    override suspend fun getAll(): List<Category> {
        TODO("Not yet implemented")
    }
}
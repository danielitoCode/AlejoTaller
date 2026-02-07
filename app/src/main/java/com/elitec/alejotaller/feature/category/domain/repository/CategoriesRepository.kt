package com.elitec.alejotaller.feature.category.domain.repository

import com.elitec.alejotaller.feature.category.domain.entity.Category

interface CategoriesRepository {
    // Se debe consumir de la BD cuando se agregue Offline first
    suspend fun getAll(): List<Category>
}
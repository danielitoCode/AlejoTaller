package com.elitec.alejotaller.feature.category.domain.repository

import com.elitec.alejotaller.feature.category.domain.entity.Category
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
    // Se debe consumir de la BD cuando se agregue Offline first
    fun observeAll(): Flow<List<Category>>
    suspend fun sync(): Result<Unit>
    suspend fun getById(id: String): Category?
}
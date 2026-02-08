package com.elitec.alejotaller.feature.category.data.repository

import com.elitec.alejotaller.feature.category.data.dao.CategoryDao
import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.category.data.mapper.toDomain
import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoriesOfflineFirstRepository(
    private val net: CategoriesNetRepositoryImpl,
    private val bd: CategoryDao
): CategoriesRepository {

    override fun observeAll(): Flow<List<Category>> =
        bd.observeAll().map { categoriesDtoList ->
            categoriesDtoList.map { categoryDto ->
                categoryDto.toDomain()
            }
        }

    override suspend fun getById(id: String): Category? = bd.getById(id)?.toDomain()

    override suspend fun sync(): Result<Unit> = runCatching {
        val remote = net.getAll()
        bd.replaceAll(remote)
    }
}
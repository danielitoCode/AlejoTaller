package com.elitec.alejotaller.feature.category.data.repository

import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD

class CategoriesOfflineFirstRepository(
    private val categoryNetRepository: CategoriesNetRepositoryImpl,
    private val bd: AppBD
): CategoriesRepository {
    private val categoryDao by lazy { bd.categoriesDao() }

    suspend fun syncProductOfCloud(): Result<Unit> = runCatching {
        val categories = categoryNetRepository.getAll()
        categoryDao.replaceAll(categories)
    }

    override suspend fun getAll(): List<Category>  = categoryDao.getAll()

    suspend fun replaceAll(items: List<Category>) {
        TODO()
    }

    suspend fun insert(item: Category) {
        TODO()
    }

    suspend fun insertAll(items: List<Category>) {
        TODO()
    }
}
package com.elitec.alejotaller.feature.category.data.repository.fakeRepository

import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.category.data.mapper.toDomain
import com.elitec.alejotaller.feature.category.di.categoryFeatureModule
import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class FakeCategoryOfflineFirstRepository : CategoriesRepository {
    private val fakeCategoryFlow = MutableStateFlow(listOf<Category>())

    private val fakeCategories = listOf(
        CategoryDto("category1", "Category test 1", "photo url 1"),
        CategoryDto("category2", "Category test 2", "photo url 2"),
        CategoryDto("category3", "Category test 3", "photo url 3")
    )

    override fun observeAll(): Flow<List<Category>> {
        return fakeCategoryFlow.asStateFlow()
    }

    override suspend fun sync(): Result<Unit> = runCatching {
        fakeCategories.forEachIndexed { index, category ->
            val lastUpdate = fakeCategoryFlow.update { list ->
                val tempCategoryList = list.toMutableList()
                tempCategoryList.add(category.toDomain())
                tempCategoryList
            }
            delay(400L * index)
        }
    }

    override suspend fun getById(id: String): Category? {
        val fakeCategoryListen = fakeCategoryFlow.last()
        return fakeCategoryListen.firstOrNull { category -> category.id == id }
    }
}
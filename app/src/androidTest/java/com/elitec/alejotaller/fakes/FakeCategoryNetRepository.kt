package com.elitec.alejotaller.fakes

import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.category.domain.repository.CategoryNetRepository

class FakeCategoryNetRepository: CategoryNetRepository {
    private val fakeCategory = listOf(
        CategoryDto("1","Category test 1", "photo url test 1"),
        CategoryDto("2","Category test 2", "photo url test 2"),
        CategoryDto("3","Category test 3", "photo url test 3"),
        CategoryDto("4","Category test 4", "photo url test 4"),
        CategoryDto("5","Category test 5", "photo url test 5"),
    )
    override suspend fun getAll(): List<CategoryDto> {
        return fakeCategory
    }
}
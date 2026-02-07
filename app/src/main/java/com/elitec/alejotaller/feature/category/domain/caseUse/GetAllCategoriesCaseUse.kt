package com.elitec.alejotaller.feature.category.domain.caseUse

import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository

class GetAllCategoriesCaseUse(
    private val repository: CategoriesRepository
) {
    // Cambiar con el offline first
    suspend operator fun invoke(): Result<List<Category>> = runCatching {
        repository.getAll()
    }
}
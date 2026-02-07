package com.elitec.alejotaller.feature.category.domain.caseUse

import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import kotlinx.coroutines.flow.Flow

class ObserveCategoriesCaseUse(
    private val repository: CategoriesRepository
) {
    operator fun invoke(): Flow<List<Category>> = repository.observeAll()
}
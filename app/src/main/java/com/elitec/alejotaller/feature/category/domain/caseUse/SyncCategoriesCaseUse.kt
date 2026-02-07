package com.elitec.alejotaller.feature.category.domain.caseUse

import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository

class SyncCategoriesCaseUse(
    private val repository: CategoriesRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.sync()
}
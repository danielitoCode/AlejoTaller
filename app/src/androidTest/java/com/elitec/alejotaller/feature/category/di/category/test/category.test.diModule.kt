package com.elitec.alejotaller.feature.category.di.category.test

import com.elitec.alejotaller.feature.category.data.repository.fakeRepository.FakeCategoryOfflineFirstRepository
import com.elitec.alejotaller.feature.category.domain.caseUse.ObserveCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.caseUse.SyncCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import com.elitec.alejotaller.feature.category.presentation.viewmodel.CategoriesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoryTestDiModule = module {
    // Data: FakeRepository
    single<CategoriesRepository> { FakeCategoryOfflineFirstRepository() }
    // Domain: CaseUse
    factory { SyncCategoriesCaseUse(get()) }
    factory { ObserveCategoriesCaseUse(get()) }
    // Presentation
    viewModel { CategoriesViewModel(get(), get()) }
}
package com.elitec.alejotaller.feature.category.di

import com.elitec.alejotaller.feature.category.data.repository.CategoriesNetRepositoryImpl
import com.elitec.alejotaller.feature.category.data.repository.CategoriesOfflineFirstRepository
import com.elitec.alejotaller.feature.category.domain.caseUse.GetAllCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import com.elitec.alejotaller.feature.category.presentation.viewmodel.CategoriesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoryFeatureModule = module {
    // Data layer
    single { CategoriesNetRepositoryImpl() }
    single<CategoriesRepository> { CategoriesOfflineFirstRepository(get(), get()) }

    // Domain layer
    factory { GetAllCategoriesCaseUse(get()) }

    // Presentation layer
    viewModel { CategoriesViewModel(get()) }
}
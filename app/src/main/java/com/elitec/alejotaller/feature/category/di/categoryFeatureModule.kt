package com.elitec.alejotaller.feature.category.di

import com.elitec.alejotaller.feature.category.data.repository.CategoriesRepositoryImpl
import com.elitec.alejotaller.feature.category.domain.caseUse.GetAllCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import com.elitec.alejotaller.feature.category.presentation.viewmodel.CategoriesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoryFeatureModule = module {
    // Data layer
    single<CategoriesRepository> { CategoriesRepositoryImpl() }

    // Domain layer
    factory { GetAllCategoriesCaseUse(get()) }

    // Presentation layer
    viewModel { CategoriesViewModel(get()) }
}
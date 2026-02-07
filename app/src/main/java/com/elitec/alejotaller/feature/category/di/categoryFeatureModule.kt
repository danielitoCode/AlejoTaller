package com.elitec.alejotaller.feature.category.di

import com.elitec.alejotaller.feature.category.data.repository.CategoriesNetRepositoryImpl
import com.elitec.alejotaller.feature.category.data.repository.CategoriesOfflineFirstRepository
import com.elitec.alejotaller.feature.category.domain.caseUse.ObserveCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.caseUse.SyncCategoriesCaseUse
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import com.elitec.alejotaller.feature.category.presentation.viewmodel.CategoriesViewModel
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoryFeatureModule = module {
    // infrastructure instances
    single { get<AppBD>().categoriesDao() }

    // Data layer
    single { CategoriesNetRepositoryImpl() }
    single<CategoriesRepository> { CategoriesOfflineFirstRepository(get(), get()) }

    // Domain layer
    factory { ObserveCategoriesCaseUse(get()) }
    factory { SyncCategoriesCaseUse(get()) }

    // Presentation layer
    viewModel { CategoriesViewModel(get(), get()) }
}
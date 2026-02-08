package com.elitec.alejotaller.feature.product.di

import com.elitec.alejotaller.feature.product.data.repository.ProductNetRepositoryImpl
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ProductViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val productFeatureModule = module {
    // Data layer
    single<ProductRepository>{ ProductNetRepositoryImpl() }

    // Domain Layer
    factory { GetProductByIdCaseUse(get()) }
    factory { SyncProductCaseUse(get()) }

    // Presentation Layer
    viewModel { ProductViewModel(get(), get()) }
}
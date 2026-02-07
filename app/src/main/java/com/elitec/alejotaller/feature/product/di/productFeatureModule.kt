package com.elitec.alejotaller.feature.product.di

import com.elitec.alejotaller.feature.product.data.repository.ProductRepositoryImpl
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ProductViewModel
import com.elitec.alejotaller.infraestructure.core.domain.Repository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val productFeatureModule = module {
    // Data layer
    single<Repository<Product>> { ProductRepositoryImpl() }

    // Domain Layer
    factory { GetProductByIdCaseUse(get()) }
    factory { SyncProductCaseUse(get()) }

    // Presentation Layer
    viewModel { ProductViewModel(get(), get()) }
}
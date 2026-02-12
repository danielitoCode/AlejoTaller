package com.elitec.alejotaller.feature.product.di

import com.elitec.alejotaller.feature.product.data.repository.ProductNetRepositoryImpl
import com.elitec.alejotaller.feature.product.data.repository.ProductOfflineFirstRepository
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ProductViewModel
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ShopCartViewModel
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val productFeatureModule = module {
    // Infrastructure instances
    single { get<AppBD>().productsDao() }

    // Data layer
    single { ProductNetRepositoryImpl(get()) }
    single<ProductRepository>{ ProductOfflineFirstRepository(get(), get()) }

    // Domain Layer
    factory { GetProductByIdCaseUse(get()) }
    factory { ObserveProductsCaseUse(get()) }
    factory { SyncProductCaseUse(get()) }

    // Presentation Layer
    viewModel { ShopCartViewModel() }
    viewModel { ProductViewModel(get(), get(), get()) }
}
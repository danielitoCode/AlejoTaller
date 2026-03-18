package com.elitec.alejotaller.feature.product.di

import com.elitec.alejotaller.feature.category.data.repository.fakeRepository.FakeCategoryOfflineFirstRepository
import com.elitec.alejotaller.feature.product.data.fakeRepository.FakeProductOfflineFirstRepository
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ProductViewModel
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ShopCartViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val productTestDiModule = module {
    // Data: FakeRepository
    single<ProductRepository> { FakeProductOfflineFirstRepository() }
    // Domain: CaseUse
    factory { ObserveProductsCaseUse(get()) }
    factory { SyncProductCaseUse(get()) }
    factory { GetProductByIdCaseUse(get()) }
    // Presentation
    viewModel { ProductViewModel(get(), get(), get()) }
    viewModel { ShopCartViewModel() }
}
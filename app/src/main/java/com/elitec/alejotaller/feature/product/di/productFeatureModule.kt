package com.elitec.alejotaller.feature.product.di

import com.elitec.alejotaller.feature.product.data.repository.ProductNetRepositoryImpl
import com.elitec.alejotaller.feature.product.data.repository.ProductOfflineFirstRepository
import com.elitec.alejotaller.feature.product.domain.caseUse.ApplySoftHoldCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.CheckAProductExistenceCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.RefreshProductsByIdsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ReleaseSoftHoldCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.repository.ProductNetRepository
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.feature.product.presentation.viewModel.ProductViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val productFeatureModule = module {
    single<ProductNetRepository> { ProductNetRepositoryImpl(get()) }
    single<ProductRepository> { ProductOfflineFirstRepository(get(), get()) }

    factory { ObserveProductsCaseUse(get()) }
    factory { SyncProductsCaseUse(get()) }
    factory { GetProductByIdCaseUse(get()) }
    factory { CheckAProductExistenceCaseUse(get()) }
    factory { ApplySoftHoldCaseUse(get()) }
    factory { ReleaseSoftHoldCaseUse(get()) }
    factory { RefreshProductsByIdsCaseUse(get()) }

    viewModel { ProductViewModel(get(), get(), get(), get()) }
}

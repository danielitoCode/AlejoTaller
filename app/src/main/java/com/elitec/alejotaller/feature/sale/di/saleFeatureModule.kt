package com.elitec.alejotaller.feature.sale.di

import com.elitec.alejotaller.feature.sale.data.repository.SaleRepositoryImpl
import com.elitec.alejotaller.feature.sale.domain.caseUse.GetAllBuyCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.feature.sale.presentation.viewmodel.SaleViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val saleFeatureModule = module {
    // Data layer
    single<SaleRepository> { SaleRepositoryImpl() }

    // Domain layer
    factory { GetAllBuyCaseUse(get()) }
    factory { RegisterNewSaleCauseUse(get()) }

    // Presentation layer
    viewModel { SaleViewModel(get(), get()) }
}
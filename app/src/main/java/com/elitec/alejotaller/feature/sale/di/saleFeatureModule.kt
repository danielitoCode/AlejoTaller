package com.elitec.alejotaller.feature.sale.di

import com.elitec.alejotaller.feature.sale.data.repository.SaleNetRepositoryImpl
import com.elitec.alejotaller.feature.sale.data.repository.SaleOfflineFirstRepository
import com.elitec.alejotaller.feature.sale.domain.caseUse.GetSalesByIdCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.SyncSalesCaseUse
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.feature.sale.presentation.viewmodel.SaleViewModel
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val saleFeatureModule = module {
    // Infrastructure instances
    single { get<AppBD>().saleDao() }
    // Data layer
    single { SaleNetRepositoryImpl(get()) }
    single<SaleRepository> { SaleOfflineFirstRepository(get(), get()) }

    // Domain layer
    factory { ObserveAllSalesCaseUse(get()) }
    factory { RegisterNewSaleCauseUse(get()) }
    factory { SyncSalesCaseUse(get()) }
    factory { GetSalesByIdCaseUse(get()) }

    // Presentation layer
    viewModel {
        SaleViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}
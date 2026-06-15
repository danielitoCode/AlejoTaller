package com.elitec.alejotaller.feature.exchange.di

import com.elitec.alejotaller.feature.exchange.data.repository.ExchangeNetRepository
import com.elitec.alejotaller.feature.exchange.data.repository.ExchangeOfflineFirstRepository
import com.elitec.alejotaller.feature.exchange.domain.caseUse.GetCachedTodayExchangeCaseUse
import com.elitec.alejotaller.feature.exchange.domain.caseUse.GetTodayExchangeCaseUse
import com.elitec.alejotaller.feature.exchange.domain.repository.ExchangeRepository
import com.elitec.alejotaller.feature.exchange.presentation.viewmodel.ExchangeViewModel
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val exchangeFeatureModule = module {
    // Data
    single { get<AppBD>().exchangeDao() }
    single { ExchangeNetRepository(get()) }
    single<ExchangeRepository> { ExchangeOfflineFirstRepository(get(), get()) }

    // Domain
    factory { GetTodayExchangeCaseUse(get()) }
    factory { GetCachedTodayExchangeCaseUse(get()) }

    // Presentation
    viewModel { ExchangeViewModel(get(), get()) }
}

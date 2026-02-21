package com.elitec.alejotaller.feature.sale.di

import com.elitec.alejotaller.feature.sale.data.repository.AppwriteSaleNotificationUserProvider
import com.elitec.alejotaller.feature.sale.data.repository.SaleNetRepositoryImpl
import com.elitec.alejotaller.feature.sale.data.repository.SaleOfflineFirstRepository
import com.elitec.alejotaller.feature.sale.data.repository.SolucionesCubaPaymentGateway
import com.elitec.alejotaller.feature.sale.data.repository.TelegramNotificatorImpl
import com.elitec.alejotaller.feature.sale.domain.caseUse.GetSalesByIdCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.InitiatePaymentCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.SubscribeRealtimeSyncCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.SyncSalesCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.UpdateDeliveryTypeCaseUse
import com.elitec.alejotaller.feature.sale.domain.repository.PaymentGateway
import com.elitec.alejotaller.feature.sale.domain.repository.SaleNotificationUserProvider
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator
import com.elitec.alejotaller.feature.sale.presentation.viewmodel.SaleViewModel
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val saleFeatureModule = module {
    // Infrastructure instances
    single { get<AppBD>().saleDao() }

    // Data layer
    single { SaleNetRepositoryImpl(get()) }
    single<PaymentGateway> { SolucionesCubaPaymentGateway(get()) }
    single<SaleRepository> { SaleOfflineFirstRepository(get(), get()) }
    single<TelegramNotificator> { TelegramNotificatorImpl(get()) }
    single<SaleNotificationUserProvider> { AppwriteSaleNotificationUserProvider(get()) }

    // Domain layer
    factory { ObserveAllSalesCaseUse(get()) }
    factory { UpdateDeliveryTypeCaseUse(get()) }
    factory { InitiatePaymentCaseUse(get(), get(), get(), get()) }
    factory { RegisterNewSaleCauseUse(get(), get(), get()) }
    factory { SyncSalesCaseUse(get()) }
    factory { GetSalesByIdCaseUse(get()) }
    factory { SubscribeRealtimeSyncCaseUse(get()) }

    // Presentation layer
    viewModel {
        SaleViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
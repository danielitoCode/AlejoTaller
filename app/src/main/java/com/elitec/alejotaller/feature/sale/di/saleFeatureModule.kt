package com.elitec.alejotaller.feature.sale.di

import com.elitec.alejotaller.feature.sale.data.repository.AppwriteSaleIdProvider
import com.elitec.alejotaller.feature.sale.data.repository.AppwriteSaleNotificationUserProvider
import com.elitec.alejotaller.feature.sale.data.repository.SolucionesCubaPaymentGateway
import com.elitec.alejotaller.feature.sale.data.repository.TelegramNotificatorImpl
import com.elitec.alejotaller.feature.sale.domain.caseUse.InitiatePaymentCaseUse
import com.elitec.alejotaller.feature.sale.presentation.viewmodel.SaleViewModel
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepositoryImpl
import com.elitec.shared.data.feature.sale.data.repository.SaleOfflineFirstRepository
import com.elitec.shared.data.infraestructure.core.data.config.SaleRemoteConfig
import com.elitec.shared.sale.feature.sale.domain.caseUse.GetSalesByIdCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.SubscribeRealtimeSyncCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.SyncSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateDeliveryTypeCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateSaleVerificationFromRealtimeCaseUse
import com.elitec.shared.sale.feature.sale.domain.repository.PaymentGateway
import com.elitec.shared.sale.feature.sale.domain.repository.SaleIdProvider
import com.elitec.shared.sale.feature.sale.domain.repository.SaleNotificationUserProvider
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository
import com.elitec.shared.sale.feature.sale.domain.repository.TelegramNotificator
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val saleFeatureModule = module {
    single { get<AppBD>().saleDao() }

    single<SaleRemoteConfig> {
        SaleRemoteConfig(
            databaseId = com.elitec.alejotaller.BuildConfig.APPWRITE_DATABASE_ID,
            saleCollectionId = com.elitec.alejotaller.BuildConfig.SALE_TABLE_ID
        )
    }

    single<SaleNetRepository> { SaleNetRepositoryImpl(get(), get()) }
    single<SaleRepository> { SaleOfflineFirstRepository(get(), get()) }

    single<SaleIdProvider> { AppwriteSaleIdProvider() }
    single<SaleNotificationUserProvider> { AppwriteSaleNotificationUserProvider(get()) }
    single<TelegramNotificator> { TelegramNotificatorImpl() }
    single<PaymentGateway> { SolucionesCubaPaymentGateway(get()) }

    factory { ObserveAllSalesCaseUse(get()) }
    factory { GetSalesByIdCaseUse(get()) }
    factory { UpdateDeliveryTypeCaseUse(get()) }
    factory { UpdateSaleVerificationFromRealtimeCaseUse(get()) }
    factory { RegisterNewSaleCauseUse(get(), get(), get(), get()) }
    factory { SyncSalesCaseUse(get()) }
    factory { SubscribeRealtimeSyncCaseUse(get()) }
    factory { InitiatePaymentCaseUse(get(), get(), get()) }

    // CheckAProductExistenceCaseUse + ApplySoftHoldCaseUse desde productFeatureModule
    viewModel {
        SaleViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}

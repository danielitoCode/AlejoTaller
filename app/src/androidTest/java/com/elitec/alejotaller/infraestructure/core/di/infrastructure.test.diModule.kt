package com.elitec.alejotaller.infraestructure.core.di

import com.elitec.alejotaller.feature.notifications.domain.caseuse.SavePromotionCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.InterpretSaleRealtimeEventCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.SubscribeRealtimeSyncCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.UpdateSaleVerificationFromRealtimeCaseUse
import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeSyncGateway
import com.elitec.alejotaller.infraestructure.core.data.realtime.FakeRealTimeManager
import com.elitec.alejotaller.infraestructure.core.data.realtime.RealTimeManagerImpl
import com.elitec.alejotaller.infraestructure.core.presentation.services.OrderNotificationService
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.RealtimeSyncViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val infrastructureTestDiModule = module {
    // Data: FakeRepository

    // Domain: Realtime Use
    single<RealtimeSyncGateway> { FakeRealTimeManager() }
    // Domain: CaseUse
    factory { SubscribeRealtimeSyncCaseUse(get()) }
    factory { SavePromotionCaseUse(get()) }
    factory { OrderNotificationService(androidContext()) }
    factory { InterpretSaleRealtimeEventCaseUse() }
    factory { UpdateSaleVerificationFromRealtimeCaseUse(get()) }

    // Presentation: ViewModels
    viewModel { RealtimeSyncViewModel(get(), get(), get(), get(), get()) }
    viewModel { ToasterViewModel() }
}


package com.elitec.alejotaller.feature.notifications.di

import com.elitec.alejotaller.feature.notifications.data.fakeRepository.FakePromotionRepository
import com.elitec.alejotaller.feature.notifications.domain.caseuse.ObserveActivePromotionsCaseUse
import com.elitec.alejotaller.feature.notifications.domain.caseuse.SavePromotionCaseUse
import com.elitec.alejotaller.feature.notifications.domain.repository.PromotionRepository
import com.elitec.alejotaller.feature.notifications.presentation.PromotionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notificationTestDiModule = module {
    // Data: FakeRepository
    single<PromotionRepository> { FakePromotionRepository() }

    // Domain: CaseUSe
    factory { ObserveActivePromotionsCaseUse(get()) }
    factory { SavePromotionCaseUse(get()) }

    // Presentation: ViewModel
    viewModel { PromotionViewModel(get()) }
}
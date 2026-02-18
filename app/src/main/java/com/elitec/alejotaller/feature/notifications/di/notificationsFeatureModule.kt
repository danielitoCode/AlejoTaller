package com.elitec.alejotaller.feature.notifications.di

import com.elitec.alejotaller.feature.notifications.data.repository.PromotionOfflineFirstRepository
import com.elitec.alejotaller.feature.notifications.domain.caseuse.ObserveActivePromotionsCaseUse
import com.elitec.alejotaller.feature.notifications.domain.caseuse.SavePromotionCaseUse
import com.elitec.alejotaller.feature.notifications.domain.repository.PromotionRepository
import com.elitec.alejotaller.feature.notifications.presentation.PromotionViewModel
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notificationsFeatureModule = module {
    single { get<AppBD>().promotionDao() }

    single<PromotionRepository> { PromotionOfflineFirstRepository(get()) }

    factory { ObserveActivePromotionsCaseUse(get()) }
    factory { SavePromotionCaseUse(get()) }

    viewModel { PromotionViewModel(get()) }
}
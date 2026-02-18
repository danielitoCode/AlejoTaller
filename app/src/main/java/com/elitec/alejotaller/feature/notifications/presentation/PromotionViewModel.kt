package com.elitec.alejotaller.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.notifications.domain.caseuse.ObserveActivePromotionsCaseUse
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class PromotionViewModel(
    observeActivePromotionsCaseUse: ObserveActivePromotionsCaseUse
) : ViewModel() {

    val promotionsFlow = observeActivePromotionsCaseUse().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
}
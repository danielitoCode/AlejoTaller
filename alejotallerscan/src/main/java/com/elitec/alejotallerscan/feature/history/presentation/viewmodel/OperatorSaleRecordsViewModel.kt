package com.elitec.alejotallerscan.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotallerscan.feature.history.domain.caseuse.ObserveOperatorSaleRecordsCaseUse
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class OperatorSaleRecordsViewModel(
    observeOperatorSaleRecordsCaseUse: ObserveOperatorSaleRecordsCaseUse
) : ViewModel() {
    val records = observeOperatorSaleRecordsCaseUse()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

package com.elitec.alejotallerscan.feature.history.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotallerscan.feature.history.domain.caseuse.ObserveOperatorSaleRecordsCaseUse
import com.elitec.alejotallerscan.feature.sync.domain.caseuse.SyncPendingOperatorSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OperatorSaleRecordsViewModel(
    observeOperatorSaleRecordsCaseUse: ObserveOperatorSaleRecordsCaseUse,
    observeAllSalesCaseUse: ObserveAllSalesCaseUse,
    private val syncPendingOperatorSalesCaseUse: SyncPendingOperatorSalesCaseUse
) : ViewModel() {
    val records = observeOperatorSaleRecordsCaseUse()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val localSales = observeAllSalesCaseUse()
        .map { sales -> sales.sortedByDescending(Sale::date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun refreshPendingSales() {
        viewModelScope.launch {
            _isSyncing.value = true
            runCatching { syncPendingOperatorSalesCaseUse() }
            _isSyncing.value = false
        }
    }
}

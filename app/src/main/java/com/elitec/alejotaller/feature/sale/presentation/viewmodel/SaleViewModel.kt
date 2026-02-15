package com.elitec.alejotaller.feature.sale.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.GetSalesByIdCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.SyncSalesCaseUse
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SaleViewModel(
    observeProductsCaseUse: ObserveAllSalesCaseUse,
    private val syncSalesCaseUse: SyncSalesCaseUse,
    private val getSaleByIdCaseUse: GetSalesByIdCaseUse,
    private val registerNewSaleCauseUse: RegisterNewSaleCauseUse
): ViewModel() {

    val salesFlow = observeProductsCaseUse().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun sync(userId: String) {
        viewModelScope.launch {
            syncSalesCaseUse(userId)
        }
    }

    fun getSaleById(id: String,onSaleCharge: (Sale?) -> Unit) {
        viewModelScope.launch {
            getSaleByIdCaseUse(id)
                .onSuccess {
                    onSaleCharge(it)
                }
        }
    }

    fun newSale(sale: Sale, onSaleRegistered: (String) -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            registerNewSaleCauseUse(sale)
                .onSuccess { transferId ->
                    onSaleRegistered(transferId)
                }
                .onFailure { error ->
                    onFail(error.message ?: "")
                }
        }
    }
}
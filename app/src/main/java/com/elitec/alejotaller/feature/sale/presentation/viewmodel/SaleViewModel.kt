package com.elitec.alejotaller.feature.sale.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.sale.domain.caseUse.GetAllBuyCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SaleViewModel(
    private val getAllBuyCaseUse: GetAllBuyCaseUse,
    private val registerNewSaleCauseUse: RegisterNewSaleCauseUse
): ViewModel() {

    fun getUserSale(userId: String, onSaleReady: (List<Sale>) -> Unit) {
        viewModelScope.launch {
            getAllBuyCaseUse(userId)
                .onSuccess { sales ->
                    onSaleReady(sales)
                }
                .onFailure {
                    onSaleReady(listOf())
                }
        }
    }

    fun newSale(sale: Sale, onSaleRegistered: () -> Unit) {
        viewModelScope.launch {
            registerNewSaleCauseUse(sale)
                .onSuccess {
                    onSaleRegistered()
                }
                .onFailure {  }
        }
    }
}
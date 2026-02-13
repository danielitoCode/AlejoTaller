package com.elitec.alejotaller.feature.product.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.entity.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    observeProductsCaseUse: ObserveProductsCaseUse,
    private val syncProductCaseUse: SyncProductCaseUse,
    private val getProductByIdCaseUse: GetProductByIdCaseUse
): ViewModel() {

    val productFlow = observeProductsCaseUse().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun syncProducts(onProductCharge: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            syncProductCaseUse()
                .onSuccess { onProductCharge() }
                .onFailure { onFail() }
        }
    }

    fun getProductById(id: String,onProductCharge: (Product?) -> Unit) {
        viewModelScope.launch {
            getProductByIdCaseUse(id)
                .onSuccess { product ->
                    onProductCharge(product)
                }
                .onFailure {
                    onProductCharge(null)
                }
        }
    }
}
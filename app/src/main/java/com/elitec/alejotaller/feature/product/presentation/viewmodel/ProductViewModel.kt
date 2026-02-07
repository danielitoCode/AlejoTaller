package com.elitec.alejotaller.feature.product.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.entity.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val syncProductCaseUse: SyncProductCaseUse,
    private val getProductByIdCaseUse: GetProductByIdCaseUse
): ViewModel() {
    private var _productFlow = MutableStateFlow(listOf<Product>())
    val productFlow get() = _productFlow.asStateFlow()

    init {
        productSync()
    }

    private fun productSync() {
        viewModelScope.launch {
            syncProductCaseUse()
                .onSuccess { product ->
                    _productFlow.value = product
                }
                .onFailure {
                    _productFlow.value = listOf()
                }
        }
    }

    private fun getProductById(id: String,onProductCharge: (Product?) -> Unit) {
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
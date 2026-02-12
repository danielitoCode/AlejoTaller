package com.elitec.alejotaller.feature.product.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShopCartViewModel: ViewModel() {
    private var _shopCartFlow = MutableStateFlow(listOf<UiSaleItem>())
    val shopCartFlow get() = _shopCartFlow.asStateFlow()

    fun addProductToACart(product: Product, quantity: Int) {
        val list = _shopCartFlow.value.toMutableList()
        list.add((UiSaleItem(product,quantity)))
        _shopCartFlow.value = list
    }

    fun removeProductFromShopCart(product: Product) {
        val list = _shopCartFlow.value.toMutableList()
        list.removeIf { item ->  item.product.id == product.id }
        _shopCartFlow.value = list
    }
}
package com.elitec.alejotaller.feature.product.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShopCartViewModel: ViewModel() {
    private val _shopCartFlow = MutableStateFlow(listOf<UiSaleItem>())
    val shopCartFlow get() = _shopCartFlow.asStateFlow()

    fun addProductToACart(product: Product, quantity: Int = 1) {
        if (quantity <= 0) return

        _shopCartFlow.update { currentItems ->
            val existing = currentItems.firstOrNull { it.product.id == product.id }
            if (existing != null) {
                currentItems.map { item ->
                    if (item.product.id == product.id) {
                        item.copy(quantity = item.quantity + quantity)
                    } else {
                        item
                    }
                }
            } else {
                currentItems + UiSaleItem(product = product, quantity = quantity)
            }
        }
    }

    fun updateProductQuantity(productId: String, quantity: Int) {
        _shopCartFlow.update { currentItems ->
            currentItems.mapNotNull { item ->
                when {
                    item.product.id != productId -> item
                    quantity <= 0 -> null
                    else -> item.copy(quantity = quantity)
                }
            }
        }
    }

    fun removeProductFromShopCart(product: Product) {
        _shopCartFlow.update { currentItems ->
            currentItems.filterNot { item -> item.product.id == product.id }
        }
    }

    fun clearCart() {
        _shopCartFlow.value = emptyList()
    }

    fun getTotalAmount(): Double =
        _shopCartFlow.value.sumOf { item -> item.product.price * item.quantity }
}
package com.elitec.alejotaller.feature.product.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Resultado de mutar cantidad en carrito (paridad web `CartQtyResult`).
 * Permite toast en UI sin bloquear el catálogo.
 */
sealed class CartQtyResult {
    data class Ok(
        val quantity: Int,
        val max: Int,
        val clamped: Boolean
    ) : CartQtyResult()

    data class Fail(
        val reason: Reason,
        val max: Int
    ) : CartQtyResult()

    enum class Reason {
        OUT_OF_STOCK,
        NOT_FOUND
    }
}

/**
 * Carrito local con tope = [Product.availableStock] (existence − reserved),
 * misma regla que `cart.store.ts` en web.
 */
class ShopCartViewModel : ViewModel() {
    private val _shopCartFlow = MutableStateFlow(listOf<UiSaleItem>())
    val shopCartFlow get() = _shopCartFlow.asStateFlow()

    private fun maxQtyFor(product: Product): Int = product.availableStock()

    private fun clampQty(product: Product, desired: Int): Int {
        val max = maxQtyFor(product)
        if (max <= 0) return 0
        return desired.coerceIn(0, max)
    }

    private fun normalizeItems(items: List<UiSaleItem>): List<UiSaleItem> =
        items.mapNotNull { item ->
            val qty = clampQty(item.product, item.quantity)
            if (qty > 0) item.copy(quantity = qty) else null
        }

    /**
     * Añade unidades respetando available.
     * Si ya hay N en carrito y max=2, solo suma hasta el tope.
     */
    fun addProductToACart(product: Product, quantity: Int = 1): CartQtyResult {
        val max = maxQtyFor(product)
        if (max <= 0) {
            return CartQtyResult.Fail(CartQtyResult.Reason.OUT_OF_STOCK, max = 0)
        }

        val add = quantity.coerceAtLeast(1)
        var result: CartQtyResult = CartQtyResult.Ok(quantity = 0, max = max, clamped = false)

        _shopCartFlow.update { currentItems ->
            val existing = currentItems.firstOrNull { it.product.id == product.id }
            val currentQty = existing?.quantity ?: 0
            val desired = currentQty + add
            val nextQty = clampQty(product, desired)
            val clamped = nextQty < desired
            result = CartQtyResult.Ok(quantity = nextQty, max = max, clamped = clamped)

            when {
                nextQty <= 0 -> currentItems.filterNot { it.product.id == product.id }
                existing != null -> currentItems.map { item ->
                    if (item.product.id == product.id) {
                        item.copy(product = product, quantity = nextQty)
                    } else {
                        item
                    }
                }
                else -> currentItems + UiSaleItem(product = product, quantity = nextQty)
            }
        }

        return result
    }

    fun updateProductQuantity(productId: String, quantity: Int): CartQtyResult {
        var result: CartQtyResult = CartQtyResult.Fail(CartQtyResult.Reason.NOT_FOUND, max = 0)

        _shopCartFlow.update { currentItems ->
            val existing = currentItems.firstOrNull { it.product.id == productId }
                ?: return@update currentItems.also {
                    result = CartQtyResult.Fail(CartQtyResult.Reason.NOT_FOUND, max = 0)
                }

            val max = maxQtyFor(existing.product)
            val desired = quantity
            val nextQty = clampQty(existing.product, desired)
            val clamped = desired > max

            result = when {
                nextQty <= 0 && desired > 0 && max <= 0 ->
                    CartQtyResult.Fail(CartQtyResult.Reason.OUT_OF_STOCK, max = 0)
                else ->
                    CartQtyResult.Ok(quantity = nextQty, max = max, clamped = clamped)
            }

            currentItems.mapNotNull { item ->
                when {
                    item.product.id != productId -> item
                    nextQty <= 0 -> null
                    else -> item.copy(quantity = nextQty)
                }
            }
        }

        return result
    }

    fun removeProductFromShopCart(product: Product) {
        _shopCartFlow.update { currentItems ->
            currentItems.filterNot { item -> item.product.id == product.id }
        }
    }

    fun clearCart() {
        _shopCartFlow.value = emptyList()
    }

    /**
     * Re-aplica topes cuando llega stock fresco de Appwrite (misma id).
     * Paridad web `refreshProductStock`.
     */
    fun refreshProductStock(products: List<Product>) {
        if (products.isEmpty()) return
        val byId = products.associateBy { it.id }
        _shopCartFlow.update { currentItems ->
            normalizeItems(
                currentItems.map { item ->
                    val fresh = byId[item.product.id]
                    if (fresh != null) item.copy(product = fresh) else item
                }
            )
        }
    }

    fun maxFor(product: Product): Int = maxQtyFor(product)

    fun getTotalAmount(): Double =
        _shopCartFlow.value.sumOf { item -> item.product.price * item.quantity }
}

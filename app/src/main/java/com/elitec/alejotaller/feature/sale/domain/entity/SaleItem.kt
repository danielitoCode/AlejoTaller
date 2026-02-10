package com.elitec.alejotaller.feature.sale.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class SaleItem(
    val productId: String,
    val quantity: Int
) {
    init {
        require(productId.isNotBlank()) { "Product id cannot be blank" }
        require(quantity > 0) { "Quantity must be greater than 0" }
    }
}
package com.elitec.alejotaller.feature.product.domain.entity

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val photoUrl: String,
    val categoryId: String,
    val rating: Double = 0.0,
    val photoLocalResource: Int? = null,
    /** Unidades físicas en almacén (no descontadas hasta VERIFIED). */
    val existence: Int = 0,
    /** Unidades comprometidas en pedidos UNVERIFIED (soft-hold). */
    val reserved: Int = 0
) {
    init {
        require(id != "") { "The value of product identifier cant not by empty" }
        require(price >= 0.0) { "The price of product identifier cant not by a negative" }
        require(existence >= 0) { "Product existence (stock) cannot be negative" }
        require(reserved >= 0) { "Product reserved stock cannot be negative" }
    }

    /** Stock vendible ahora = existence - reserved */
    fun availableStock(): Int = (existence - reserved).coerceAtLeast(0)
}

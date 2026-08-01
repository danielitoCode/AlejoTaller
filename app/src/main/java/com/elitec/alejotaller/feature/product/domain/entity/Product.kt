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
    /** Unidades disponibles (paridad con web `existence` / Appwrite). */
    val existence: Int = 0
) {
    init {
        require(id != "") { "The value of product identifier cant not by empty" }
        require(price >= 0.0) { "The price of product identifier cant not by a negative" }
        require(existence >= 0) { "Product existence (stock) cannot be negative" }
    }
}

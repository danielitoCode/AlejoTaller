package com.elitec.alejotaller.feature.product.domain.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    // @PrimaryKey val id: String,
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    @SerialName("photo_url")
    val photoUrl: String,
    @SerialName("categoria_id")
    val categoriaId: String
) {
    init {
        require(id != "") { "The value of product identifier cant not by empty" }
        require(price < 0.0) { "The price of product identifier cant not by a negative" }
    }
}

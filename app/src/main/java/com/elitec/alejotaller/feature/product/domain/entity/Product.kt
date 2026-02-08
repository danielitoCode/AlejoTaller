package com.elitec.alejotaller.feature.product.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elitec.alejotaller.infraestructure.core.domain.entity.CoreEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Product(
    override val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val photoUrl: String,
    val categoryId: String,
    val rating: Double = 0.0,
    val photoLocalResource: Int? = null
): CoreEntity {
    init {
        require(id != "") { "The value of product identifier cant not by empty" }
        require(price >= 0.0) { "The price of product identifier cant not by a negative" }
    }
}

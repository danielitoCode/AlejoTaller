package com.elitec.alejotaller.feature.product.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elitec.alejotaller.infraestructure.core.data.mappers.AutoMapper
import com.elitec.alejotaller.infraestructure.core.domain.entity.CoreEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
@AutoMapper(
    domain = "com.elitec.alejotaller.feature.product.domain.entity.Product"
)
data class ProductDto(
    @PrimaryKey override val id: String,
    val name: String,
    val description: String,
    val price: Double,
    @SerialName("photo_url")
    val photoUrl: String,
    @SerialName("category_id")
    val categoryId: String,
    val rating: Double = 0.0,
    val photoLocalResource: Int? = null
): CoreEntity {
    init {
        require(id != "") { "The value of product identifier cant not by empty" }
        require(price >= 0.0) { "The price of product identifier cant not by a negative" }
    }
}
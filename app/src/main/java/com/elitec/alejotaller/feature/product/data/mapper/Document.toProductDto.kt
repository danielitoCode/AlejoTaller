package com.elitec.alejotaller.feature.product.data.mapper

import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import io.appwrite.models.Document

fun  Document<Map<String, Any>>.toProductDto(): ProductDto =
    ProductDto(
        id = this.id,
        name = (data["name"] as? String) ?: "Sin nombre",
        photoUrl = (data["photo_url"] as? String) ?: "",
        description = (data["description"] as? String) ?: "",
        price = (data["price"] as? Number)?.toDouble() ?: 0.0,
        categoryId = (data["category_id"] as? String) ?: "",
        rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
        photoLocalResource = 1
    )
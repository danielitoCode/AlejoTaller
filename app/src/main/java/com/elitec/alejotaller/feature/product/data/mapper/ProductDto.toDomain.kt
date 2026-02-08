package com.elitec.alejotaller.feature.product.data.mapper

import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.domain.entity.Product

fun ProductDto.toDomain(): Product =
    Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        photoUrl = TODO(),
        categoryId = TODO(),
        rating = TODO(),
        photoLocalResource = TODO()
    )
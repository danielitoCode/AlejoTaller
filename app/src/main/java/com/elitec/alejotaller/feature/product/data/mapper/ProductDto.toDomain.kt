package com.elitec.alejotaller.feature.product.data.mapper

import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.domain.entity.Product

fun ProductDto.toDomain(): Product =
    Product(
        id = id,
        name = name,
        description = description,
        price = price,
        photoUrl = photoUrl,
        categoryId = categoryId,
        rating = rating,
        photoLocalResource = 1
    )
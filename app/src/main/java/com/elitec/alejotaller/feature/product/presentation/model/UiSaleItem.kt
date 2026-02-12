package com.elitec.alejotaller.feature.product.presentation.model

import com.elitec.alejotaller.feature.product.domain.entity.Product

data class UiSaleItem(
    val product: Product,
    val quantity: Int
)
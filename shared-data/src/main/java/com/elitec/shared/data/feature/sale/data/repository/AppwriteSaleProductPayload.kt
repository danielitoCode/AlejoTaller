package com.elitec.shared.data.feature.sale.data.repository

import kotlinx.serialization.Serializable

@Serializable
data class AppwriteSaleProductPayload(
    val productId: String,
    val quantity: Int,
    val price: Double? = null
)
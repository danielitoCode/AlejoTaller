package com.elitec.alejotaller.feature.sale.data.model

import kotlinx.serialization.Serializable

@Serializable
sealed class SaleEventResponse {
    @Serializable
    data class SaleSuccessResponse(
        val saleId: String
    ): SaleEventResponse()

    @Serializable
    data class SaleErrorResponse(
        val saleId: String,
        val cause: String
    ): SaleEventResponse()
}


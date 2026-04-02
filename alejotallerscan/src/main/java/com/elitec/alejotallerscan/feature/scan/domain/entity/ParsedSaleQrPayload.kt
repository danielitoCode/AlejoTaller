package com.elitec.alejotallerscan.feature.scan.domain.entity

data class ParsedSaleQrPayload(
    val saleId: String,
    val userId: String? = null,
    val amount: Double? = null,
    val items: List<ParsedSaleQrItem> = emptyList(),
    val rawPayload: String
)

data class ParsedSaleQrItem(
    val productId: String,
    val quantity: Int,
    val unitPrice: Double? = null
)

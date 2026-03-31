package com.elitec.shared.sale.feature.sale.domain.realtime

data class SaleRealtimeEvent(
    val saleId: String,
    val userId: String,
    val isSuccess: Boolean,
    val cause: String? = null
)

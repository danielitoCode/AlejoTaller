package com.elitec.alejotaller.feature.exchange.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyRateDTO(
    val CUP: Float? = null,
    val MLC: Float? = null,
    val USD: Float? = null,
    val EUR: Float? = null
)

@Serializable
data class CupExchangeDTO(
    val ok: Boolean,
    val fecha: String,
    val hora: String,
    val actualizado: String? = null,
    val tasas: Map<String, CurrencyRateDTO>
)

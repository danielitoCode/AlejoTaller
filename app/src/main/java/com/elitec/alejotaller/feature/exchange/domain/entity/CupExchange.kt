package com.elitec.alejotaller.feature.exchange.domain.entity

data class CupExchange(
    val id: String,
    val usdReference: Float,
    val euroReference: Float,
    val updateAt: String,
    val source: String = "DIRECTORIO CUBANO"
) {
    init {
        require(usdReference > 0)
        require(euroReference > 0)
    }
}

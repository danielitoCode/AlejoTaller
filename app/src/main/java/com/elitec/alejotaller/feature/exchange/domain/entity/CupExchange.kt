package com.elitec.alejotaller.feature.exchange.domain.entity

data class CupExchange(
    val id: String,
    val usdReference: Float,
    val euroReference: Float,
    val updatedAt: String,
    val source: String = "DIRECTORIO_CUBANO"
) {
    init {
        require(usdReference > 0) { "The value of USD exchange must be positive" }
        require(euroReference > 0) { "The value of EUR exchange must be positive" }
        require(id.isNotBlank()) { "The value of exchange identifier cannot be empty" }
    }
}

package com.elitec.alejotaller.feature.sale.domain.entity

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class Sale(
    val id: String,
    val date: LocalDate,
    val amount: Double,
    val verified: BuyState,
    val products: List<SaleItem>,
    val userId: String
)

enum class BuyState {
    UNVERIFIED, VERIFIED, DELETED
}


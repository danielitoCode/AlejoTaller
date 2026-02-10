package com.elitec.alejotaller.feature.sale.domain.entity

import kotlinx.datetime.LocalDate

data class Sale(
    val id: String,
    val date: LocalDate,
    val amount: Double,
    val products: List<SaleItem>,
    val userId: String
)
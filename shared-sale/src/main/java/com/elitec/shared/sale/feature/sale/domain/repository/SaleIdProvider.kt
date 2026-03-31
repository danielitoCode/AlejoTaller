package com.elitec.shared.sale.feature.sale.domain.repository

interface SaleIdProvider {
    fun nextId(): String
}

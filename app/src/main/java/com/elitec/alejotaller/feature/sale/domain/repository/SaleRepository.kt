package com.elitec.alejotaller.feature.sale.domain.repository

import com.elitec.alejotaller.feature.sale.domain.entity.Sale

interface SaleRepository {
    suspend fun getAll(userId: String): List<Sale>
    suspend fun getById(itemId: String): Sale
    suspend fun save(item: Sale, onSave: () -> Unit = {})
}
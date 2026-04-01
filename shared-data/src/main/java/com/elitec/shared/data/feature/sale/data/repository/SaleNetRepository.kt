package com.elitec.shared.data.feature.sale.data.repository

import com.elitec.shared.data.feature.sale.data.dto.SaleDto

interface SaleNetRepository {
    suspend fun getAll(userId: String): List<SaleDto>
    suspend fun getById(itemId: String): SaleDto
    suspend fun search(field: String, query: String, limit: Int = 50): List<SaleDto>
    suspend fun save(item: SaleDto)
    suspend fun upsert(item: SaleDto)
}

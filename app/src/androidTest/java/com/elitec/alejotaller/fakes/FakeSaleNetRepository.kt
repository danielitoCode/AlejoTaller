package com.elitec.alejotaller.fakes

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleNetRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class FakeSaleNetRepository(
    seed: List<SaleDto> = emptyList()
) : SaleNetRepository {


    private val salesById = linkedMapOf<String, SaleDto>().apply {
        seed.forEach { put(it.id, it) }
    }

    override suspend fun getAll(userId: String): List<SaleDto> =
        salesById.values.filter { it.userId == userId }

    override suspend fun getById(itemId: String): SaleDto =
        salesById[itemId] ?: throw NoSuchElementException("Sale with id '$itemId' was not found")

    override suspend fun save(item: SaleDto) {
        require(!salesById.containsKey(item.id)) { "Sale with id '${item.id}' already exists" }
        salesById[item.id] = item
    }

    override suspend fun upsert(item: SaleDto) {
        salesById[item.id] = item
    }
}
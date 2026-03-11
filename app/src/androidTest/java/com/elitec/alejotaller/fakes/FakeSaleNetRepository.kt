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

class FakeSaleNetRepository: SaleNetRepository {


    override suspend fun getAll(userId: String): List<SaleDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(itemId: String): SaleDto {
        TODO("Not yet implemented")
    }

    override suspend fun save(item: SaleDto) {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(item: SaleDto) {
        TODO("Not yet implemented")
    }
}
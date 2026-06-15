package com.elitec.alejotaller.feature.exchange.data.repository

import com.elitec.alejotaller.feature.exchange.data.dao.ExchangeDao
import com.elitec.alejotaller.feature.exchange.data.mapper.toDomain
import com.elitec.alejotaller.feature.exchange.data.mapper.toLocalDto
import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange
import com.elitec.alejotaller.feature.exchange.domain.repository.ExchangeRepository

class ExchangeOfflineFirstRepository(
    private val net: ExchangeNetRepository,
    private val dao: ExchangeDao
) : ExchangeRepository {

    private val TODAY_CACHE_ID = "directorioCubano-today"

    override suspend fun getToday(): CupExchange {
        return try {
            val remoteDto = net.getExchangeToday()
            val domain = remoteDto.toDomain()
            // In Web they use TODAY_CACHE_ID as id for the daily one in some places, 
            // but the domain id is derived from date. Let's follow Web's getToday logic:
            // await db.exchangeRates.put({ ...remote, id: TODAY_CACHE_ID });
            dao.insertExchange(domain.copy(id = TODAY_CACHE_ID).toLocalDto())
            domain
        } catch (e: Exception) {
            dao.getExchangeById(TODAY_CACHE_ID)?.toDomain() ?: throw e
        }
    }

    override suspend fun getCachedToday(): CupExchange? {
        return dao.getExchangeById(TODAY_CACHE_ID)?.toDomain()
    }
}

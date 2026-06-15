package com.elitec.alejotaller.feature.exchange.domain.repository

import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange

interface ExchangeRepository {
    suspend fun getToday(): CupExchange
    suspend fun getCachedToday(): CupExchange?
}

package com.elitec.alejotaller.feature.exchange.domain.repository

import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange

interface ExchangeRepository {
    fun getToday(): CupExchange
    fun getCachedToday(): CupExchange?
}
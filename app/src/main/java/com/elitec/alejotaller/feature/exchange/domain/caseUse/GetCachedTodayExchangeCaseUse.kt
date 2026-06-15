package com.elitec.alejotaller.feature.exchange.domain.caseUse

import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange
import com.elitec.alejotaller.feature.exchange.domain.repository.ExchangeRepository

class GetCachedTodayExchangeCaseUse(
    private val repository: ExchangeRepository
) {
    suspend operator fun invoke(): CupExchange? = repository.getCachedToday()
}

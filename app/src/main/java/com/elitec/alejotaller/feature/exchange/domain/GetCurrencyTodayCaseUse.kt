package com.elitec.alejotaller.feature.exchange.domain

import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange
import com.elitec.alejotaller.feature.exchange.domain.repository.ExchangeRepository

class GetCurrencyTodayCaseUse(
    private val exchangeRepository: ExchangeRepository
) {
    suspend operator fun invoke(): Result<CupExchange> =  runCatching {
         exchangeRepository.getToday()
    }
}
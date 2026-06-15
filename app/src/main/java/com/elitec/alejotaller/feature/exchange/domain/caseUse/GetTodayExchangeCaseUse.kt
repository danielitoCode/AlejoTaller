package com.elitec.alejotaller.feature.exchange.domain.caseUse

import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange
import com.elitec.alejotaller.feature.exchange.domain.repository.ExchangeRepository

class GetTodayExchangeCaseUse(
    private val repository: ExchangeRepository
) {
    suspend operator fun invoke(): Result<CupExchange> = runCatching {
        repository.getToday()
    }
}

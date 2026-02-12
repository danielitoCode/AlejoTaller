package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator

class RegisterNewSaleCauseUse(
    private val repository: SaleRepository,
    private val telegramNotificator: TelegramNotificator
) {
    suspend operator fun invoke(sale: Sale): Result<Unit> =  runCatching {
        repository.save(sale)
    }
}

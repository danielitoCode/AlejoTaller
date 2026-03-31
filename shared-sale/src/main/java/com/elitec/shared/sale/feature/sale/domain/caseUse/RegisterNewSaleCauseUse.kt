package com.elitec.shared.sale.feature.sale.domain.caseUse

import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.repository.SaleIdProvider
import com.elitec.shared.sale.feature.sale.domain.repository.SaleNotificationUserProvider
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository
import com.elitec.shared.sale.feature.sale.domain.repository.TelegramNotificator

class RegisterNewSaleCauseUse(
    private val repository: SaleRepository,
    private val saleIdProvider: SaleIdProvider,
    private val notificationUserProvider: SaleNotificationUserProvider,
    private val telegramNotificator: TelegramNotificator
) {
    suspend operator fun invoke(sale: Sale): Result<String> = runCatching {
        val saleConfirmed = sale.copy(id = saleIdProvider.nextId())

        val user = notificationUserProvider
            .getCurrentUser()
            .getOrElse { throw Exception("Transfer fail") }

        telegramNotificator.notify(saleConfirmed, user)


        repository.save(saleConfirmed)

        saleConfirmed.id
    }
}

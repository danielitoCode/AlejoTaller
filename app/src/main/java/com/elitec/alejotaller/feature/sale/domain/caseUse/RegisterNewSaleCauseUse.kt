package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleNotificationUserProvider
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator
import io.appwrite.ID

class RegisterNewSaleCauseUse(
    private val repository: SaleRepository,
    private val notificationUserProvider: SaleNotificationUserProvider,
    private val telegramNotificator: TelegramNotificator
) {
    suspend operator fun invoke(sale: Sale): Result<String> = runCatching {
        val saleConfirmed = sale.copy(id = ID.unique())

        val user = notificationUserProvider
            .getCurrentUser()
            .getOrElse { throw Exception("Transfer fail") }

        telegramNotificator.notify(saleConfirmed, user)


        repository.save(saleConfirmed)

        sale.id
    }
}
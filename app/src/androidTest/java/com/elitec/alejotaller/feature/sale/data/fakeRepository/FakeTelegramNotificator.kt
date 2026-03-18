package com.elitec.alejotaller.feature.sale.data.fakeRepository

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleNotifierUser
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator

class FakeTelegramNotificator: TelegramNotificator {
    override suspend fun notify(
        sale: Sale,
        user: SaleNotifierUser,
    ) {}
}
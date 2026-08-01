package com.elitec.shared.sale.feature.sale.fakes

import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleNotifierUser
import com.elitec.shared.sale.feature.sale.domain.repository.SaleIdProvider
import com.elitec.shared.sale.feature.sale.domain.repository.SaleNotificationUserProvider
import com.elitec.shared.sale.feature.sale.domain.repository.TelegramNotificator

class FakeSaleIdProvider(
    private val nextValue: String = "generated-sale-id"
) : SaleIdProvider {
    override fun nextId(): String = nextValue
}

class FakeSaleNotificationUserProvider(
    private val result: Result<SaleNotifierUser> = Result.success(
        SaleNotifierUser("Operator", "operator@alejo.dev", "+5350000000")
    )
) : SaleNotificationUserProvider {
    override suspend fun getCurrentUser(): Result<SaleNotifierUser> = result
}

class FakeTelegramNotificator(
    private val shouldFail: Boolean = false
) : TelegramNotificator {
    var notifiedSale: Sale? = null
    var notifiedUser: SaleNotifierUser? = null
    var notifyCalls: Int = 0

    override suspend fun notify(sale: Sale, user: SaleNotifierUser) {
        notifyCalls++
        if (shouldFail) {
            throw IllegalStateException("Telegram unavailable (test)")
        }
        notifiedSale = sale
        notifiedUser = user
    }
}

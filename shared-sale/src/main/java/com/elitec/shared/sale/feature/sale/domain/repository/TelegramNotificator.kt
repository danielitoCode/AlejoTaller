package com.elitec.shared.sale.feature.sale.domain.repository

import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleNotifierUser

interface TelegramNotificator {
    suspend fun notify(sale: Sale, user: SaleNotifierUser)
}

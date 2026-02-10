package com.elitec.alejotaller.feature.sale.domain.repository

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.sale.domain.entity.Sale

interface TelegramNotificator {
    suspend fun notify(sale: Sale, user: User)
}
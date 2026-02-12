package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.feature.sale.domain.entity.SaleNotifierUser
import com.elitec.alejotaller.feature.sale.domain.repository.SaleNotificationUserProvider
import io.appwrite.services.Account

class AppwriteSaleNotificationUserProvider(
    private val account: Account
) : SaleNotificationUserProvider {
    override suspend fun getCurrentUser(): Result<SaleNotifierUser> = runCatching {
        val user = account.get()
        val prefs = account.getPrefs().data

        SaleNotifierUser(
            name = user.name,
            email = user.email,
            phone = prefs["phone"] as? String
        )
    }
}
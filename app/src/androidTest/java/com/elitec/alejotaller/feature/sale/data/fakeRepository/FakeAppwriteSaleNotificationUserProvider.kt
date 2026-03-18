package com.elitec.alejotaller.feature.sale.data.fakeRepository

import com.elitec.alejotaller.feature.sale.domain.entity.SaleNotifierUser
import com.elitec.alejotaller.feature.sale.domain.repository.SaleNotificationUserProvider

class FakeAppwriteSaleNotificationUserProvider: SaleNotificationUserProvider {
    override suspend fun getCurrentUser(): Result<SaleNotifierUser> = Result.success(
        SaleNotifierUser(
            name = "User Name test",
            email = "example@gmail.com",
            phone = "+54555555"
        )
    )
}
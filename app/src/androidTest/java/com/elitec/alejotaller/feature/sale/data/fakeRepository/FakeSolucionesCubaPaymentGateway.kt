package com.elitec.alejotaller.feature.sale.data.fakeRepository

import com.elitec.alejotaller.feature.sale.domain.repository.PaymentGateway

class FakeSolucionesCubaPaymentGateway: PaymentGateway {
    override suspend fun createCheckoutUrl(
        saleId: String,
        amount: Double,
        description: String,
    ): Result<String> = Result.success("payment key value")
}
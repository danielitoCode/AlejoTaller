package com.elitec.shared.sale.feature.sale.domain.caseUse

import com.elitec.shared.sale.feature.sale.fakes.FakeSaleIdProvider
import com.elitec.shared.sale.feature.sale.fakes.FakeSaleNotificationUserProvider
import com.elitec.shared.sale.feature.sale.fakes.FakeSaleRepository
import com.elitec.shared.sale.feature.sale.fakes.FakeTelegramNotificator
import com.elitec.shared.sale.feature.sale.fakes.sampleSale
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class RegisterNewSaleCauseUseTest {

    @Test
    fun `genera id notifica y guarda la venta`() = runTest {
        val repository = FakeSaleRepository()
        val notificator = FakeTelegramNotificator()
        val caseUse = RegisterNewSaleCauseUse(
            repository = repository,
            saleIdProvider = FakeSaleIdProvider("sale-generated"),
            notificationUserProvider = FakeSaleNotificationUserProvider(),
            telegramNotificator = notificator
        )

        val result = caseUse(sampleSale(id = ""))

        assertTrue(result.isSuccess)
        assertEquals("sale-generated", result.getOrThrow())
        assertEquals("sale-generated", repository.currentSale.id)
        assertEquals("sale-generated", notificator.notifiedSale?.id)
    }
}

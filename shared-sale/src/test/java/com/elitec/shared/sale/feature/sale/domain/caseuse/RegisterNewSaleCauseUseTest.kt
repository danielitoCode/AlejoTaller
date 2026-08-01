package com.elitec.shared.sale.feature.sale.domain.caseuse

import com.elitec.shared.sale.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
import com.elitec.shared.sale.feature.sale.fakes.FakeSaleIdProvider
import com.elitec.shared.sale.feature.sale.fakes.FakeSaleNotificationUserProvider
import com.elitec.shared.sale.feature.sale.fakes.FakeSaleRepository
import com.elitec.shared.sale.feature.sale.fakes.FakeTelegramNotificator
import com.elitec.shared.sale.feature.sale.fakes.sampleSale
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterNewSaleCauseUseTest {

    @Test
    fun `run id notification and save the sale`() = runTest {
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

    @Test
    fun `save sale even when telegram notification fails`() = runTest {
        val repository = FakeSaleRepository()
        val notificator = FakeTelegramNotificator(shouldFail = true)
        val caseUse = RegisterNewSaleCauseUse(
            repository = repository,
            saleIdProvider = FakeSaleIdProvider("sale-despite-telegram"),
            notificationUserProvider = FakeSaleNotificationUserProvider(),
            telegramNotificator = notificator
        )

        val result = caseUse(sampleSale(id = ""))

        assertTrue(
            "El checkout no debe fallar por Telegram", 
            result.isSuccess
        )
        assertEquals("sale-despite-telegram", result.getOrThrow())
        assertEquals("sale-despite-telegram", repository.currentSale.id)
        assertNull(notificator.notifiedSale)
        assertEquals(1, notificator.notifyCalls)
    }

    @Test
    fun `save sale even when notification user provider fails`() = runTest {
        val repository = FakeSaleRepository()
        val notificator = FakeTelegramNotificator()
        val caseUse = RegisterNewSaleCauseUse(
            repository = repository,
            saleIdProvider = FakeSaleIdProvider("sale-no-user"),
            notificationUserProvider = FakeSaleNotificationUserProvider(
                result = Result.failure(IllegalStateException("No session"))
            ),
            telegramNotificator = notificator
        )

        val result = caseUse(sampleSale(id = ""))

        assertTrue(result.isSuccess)
        assertEquals("sale-no-user", result.getOrThrow())
        assertEquals("sale-no-user", repository.currentSale.id)
        assertNull(notificator.notifiedSale)
    }
}

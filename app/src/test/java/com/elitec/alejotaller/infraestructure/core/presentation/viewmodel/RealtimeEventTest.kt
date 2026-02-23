package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class RealtimeSaleEventFilterTest {

    @Test
    fun `accepts only when sale belongs to active user and pending ids`() {
        val event = SaleRealtimeEvent(
            saleId = "sale-1",
            userId = "user-a",
            isSuccess = true
        )

        val accepted = shouldHandleSaleEvent(
            event = event,
            activeUserId = "user-a",
            activePendingSaleIds = setOf("sale-1", "sale-2")
        )

        assertTrue(accepted)
    }

    @Test
    fun `rejects event from another user`() {
        val event = SaleRealtimeEvent(
            saleId = "sale-1",
            userId = "user-b",
            isSuccess = true
        )

        val accepted = shouldHandleSaleEvent(
            event = event,
            activeUserId = "user-a",
            activePendingSaleIds = setOf("sale-1")
        )

        assertFalse(accepted)
    }

    @Test
    fun `rejects event if sale id is not pending`() {
        val event = SaleRealtimeEvent(
            saleId = "sale-9",
            userId = "user-a",
            isSuccess = false,
            cause = "declined"
        )

        val accepted = shouldHandleSaleEvent(
            event = event,
            activeUserId = "user-a",
            activePendingSaleIds = setOf("sale-1", "sale-2")
        )

        assertFalse(accepted)
    }
}
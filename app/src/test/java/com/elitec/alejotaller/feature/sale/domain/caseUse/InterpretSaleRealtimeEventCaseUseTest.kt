package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeMessageKind
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeCommand
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent
import junit.framework.TestCase
import org.junit.Test
import kotlin.test.assertEquals

class InterpretSaleRealtimeEventCaseUseTest {
    private val interpretSaleRealtimeEventCaseUse =
        InterpretSaleRealtimeEventCaseUse()

    @Test
    fun `event with empty saleId test`() {
        // Given
        val event = SaleRealtimeEvent(
            saleId = "",
            userId = "user test",
            isSuccess = false,
            cause = "Account invalid access"
        )
        // When
        val response = interpretSaleRealtimeEventCaseUse(event)
        // Then
        assertEquals(emptyList(), response)
    }

    @Test
    fun `event with empty userId test`() {
        // Given
        val event = SaleRealtimeEvent(
            saleId = "Sale test",
            userId = "",
            isSuccess = false,
            cause = "Account invalid access"
        )
        // When
        val response = interpretSaleRealtimeEventCaseUse(event)
        // Then
        assertEquals(emptyList(), response)
    }

    @Test
    fun `event with empty userId and saleId test`() {
        // Given
        val event = SaleRealtimeEvent(
            saleId = "",
            userId = "",
            isSuccess = false,
            cause = "Account invalid access"
        )
        // When
        val response = interpretSaleRealtimeEventCaseUse(event)
        // Then
        assertEquals(emptyList(), response)
    }

    @Test
    fun `event fail result test`() {
        // Given
        val event = SaleRealtimeEvent(
            saleId = "sale test",
            userId = "user test",
            isSuccess = false,
            cause = "Cause failed description test"
        )
        // When
        val response = interpretSaleRealtimeEventCaseUse(event)
        // Then
        val referenceResult = listOf(
            SaleRealtimeCommand.InAppMessage(
                message = "Pedido ${event.saleId} rechazado: ${event.cause ?: "sin detalles"}",
                kind = RealtimeMessageKind.Error
            ),
            SaleRealtimeCommand.PushNotification(
                title = "Pedido con incidencia",
                body = "Revisa el pedido ${event.saleId}"
            )
        )
        assertEquals(referenceResult, response)
    }

    @Test
    fun `event success result test`() {
        // Given
        val event = SaleRealtimeEvent(
            saleId = "sale test",
            userId = "user test",
            isSuccess = true,
            cause = ""
        )
        // When
        val response = interpretSaleRealtimeEventCaseUse(event)
        // Then
        val referenceResult = listOf(
            SaleRealtimeCommand.InAppMessage(
                message = "Pedido ${event.saleId} confirmado",
                kind = RealtimeMessageKind.Success
            ),
            SaleRealtimeCommand.PushNotification(
                title = "Pedido confirmado",
                body = "Tu pedido ${event.saleId} fue confirmado"
            )
        )
        assertEquals(referenceResult, response)
    }
}
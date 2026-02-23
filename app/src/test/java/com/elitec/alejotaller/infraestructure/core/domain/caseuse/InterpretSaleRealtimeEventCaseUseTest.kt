package com.elitec.alejotaller.infraestructure.core.domain.caseuse

import com.elitec.alejotaller.feature.sale.domain.caseUse.InterpretSaleRealtimeEventCaseUse
import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeMessageKind
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeCommand
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InterpretSaleRealtimeEventCaseUseTest {

    private val caseUse = InterpretSaleRealtimeEventCaseUse()

    @Test
    fun `returns success in-app message and push command when event is success`() {
        val commands = caseUse(
            SaleRealtimeEvent(
                saleId = "sale-123",
                userId = "user-1",
                isSuccess = true
            )
        )

        assertEquals(2, commands.size)

        val inAppMessage = commands[0] as SaleRealtimeCommand.InAppMessage
        assertEquals("Pedido sale-123 confirmado", inAppMessage.message)
        assertEquals(RealtimeMessageKind.Success, inAppMessage.kind)

        val push = commands[1] as SaleRealtimeCommand.PushNotification
        assertEquals("Pedido confirmado", push.title)
        assertEquals("Tu pedido sale-123 fue confirmado", push.body)
    }

    @Test
    fun `returns error in-app message and push command when event fails`() {
        val commands = caseUse(
            SaleRealtimeEvent(
                saleId = "sale-404",
                userId = "user-1",
                isSuccess = false,
                cause = "payment timeout"
            )
        )

        assertEquals(2, commands.size)
        assertTrue(commands[0] is SaleRealtimeCommand.InAppMessage)
        assertTrue(commands[1] is SaleRealtimeCommand.PushNotification)

        val inAppMessage = commands[0] as SaleRealtimeCommand.InAppMessage
        assertEquals("Pedido sale-404 rechazado: payment timeout", inAppMessage.message)
        assertEquals(RealtimeMessageKind.Error, inAppMessage.kind)

        val push = commands[1] as SaleRealtimeCommand.PushNotification
        assertEquals("Pedido con incidencia", push.title)
        assertEquals("Revisa el pedido sale-404", push.body)
    }
}
package com.elitec.shared.sale.feature.sale.domain.caseuse

import com.elitec.shared.sale.feature.sale.domain.caseUse.InterpretSaleRealtimeEventCaseUse
import com.elitec.shared.sale.feature.sale.domain.realtime.RealtimeMessageKind
import com.elitec.shared.sale.feature.sale.domain.realtime.SaleRealtimeCommand
import com.elitec.shared.sale.feature.sale.domain.realtime.SaleRealtimeEvent
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class InterpretSaleRealtimeEventCaseUseTest {

    @Test
    fun `run a success message and push notification for confirmed events`() {
        val commands = InterpretSaleRealtimeEventCaseUse()(
            SaleRealtimeEvent(
                saleId = "sale-1",
                userId = "user-1",
                isSuccess = true,
                cause = null
            )
        )

        assertEquals(2, commands.size)
        assertEquals(
            SaleRealtimeCommand.InAppMessage("Pedido sale-1 confirmado", RealtimeMessageKind.Success),
            commands[0]
        )
        assertEquals(
            SaleRealtimeCommand.PushNotification("Pedido confirmado", "Tu pedido sale-1 fue confirmado"),
            commands[1]
        )
    }

    @Test
    fun `denied event with not necessary id`() {
        val commands = InterpretSaleRealtimeEventCaseUse()(
            SaleRealtimeEvent(
                saleId = "",
                userId = "user-1",
                isSuccess = true,
                cause = null
            )
        )

        assertTrue(commands.isEmpty())
    }
}

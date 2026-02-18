package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeMessageKind
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeCommand
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent

class InterpretSaleRealtimeEventCaseUse {
    operator fun invoke(event: SaleRealtimeEvent): List<SaleRealtimeCommand> {
        return if (event.isSuccess) {
            listOf(
                SaleRealtimeCommand.InAppMessage(
                    message = "Pedido ${event.saleId} confirmado",
                    kind = RealtimeMessageKind.Success
                ),
                SaleRealtimeCommand.PushNotification(
                    title = "Pedido confirmado",
                    body = "Tu pedido ${event.saleId} fue confirmado"
                )
            )
        } else {
            listOf(
                SaleRealtimeCommand.InAppMessage(
                    message = "Pedido ${event.saleId} rechazado: ${event.cause ?: "sin detalles"}",
                    kind = RealtimeMessageKind.Error
                ),
                SaleRealtimeCommand.PushNotification(
                    title = "Pedido con incidencia",
                    body = "Revisa el pedido ${event.saleId}"
                )
            )
        }
    }
}
package com.elitec.alejotaller.feature.sale.domain.realtime

sealed interface SaleRealtimeCommand {
    data class InAppMessage(
        val message: String,
        val kind: RealtimeMessageKind
    ) : SaleRealtimeCommand

    data class PushNotification(
        val title: String,
        val body: String
    ) : SaleRealtimeCommand
}
package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeSyncGateway
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent

class SubscribeRealtimeSyncCaseUse(
    private val gateway: RealtimeSyncGateway
) {
    operator fun invoke(
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit = {}
    ) {
        gateway.subscribe(
            onConnect = onConnect,
            onDisconnect = onDisconnect,
            onSaleEvent = onSaleEvent,
            onPromotion = onPromotion
        )
    }

    fun unsubscribeAll() {
        gateway.unsubscribeAll()
    }
}
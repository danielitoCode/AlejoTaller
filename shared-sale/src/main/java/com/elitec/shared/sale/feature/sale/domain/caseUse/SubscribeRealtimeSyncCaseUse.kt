package com.elitec.shared.sale.feature.sale.domain.caseUse

import com.elitec.shared.core.feature.notifications.domain.entity.Promotion
import com.elitec.shared.sale.feature.sale.domain.realtime.RealtimeSyncGateway
import com.elitec.shared.sale.feature.sale.domain.realtime.SaleRealtimeEvent

class SubscribeRealtimeSyncCaseUse(
    private val gateway: RealtimeSyncGateway
) {
    operator fun invoke(
        userId: String,
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit = {}
    ) {
        gateway.subscribe(
            userId = userId,
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

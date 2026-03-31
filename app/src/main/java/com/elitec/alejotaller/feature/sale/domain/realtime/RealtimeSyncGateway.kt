package com.elitec.alejotaller.feature.sale.domain.realtime

import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.shared.sale.feature.sale.domain.realtime.SaleRealtimeEvent

interface RealtimeSyncGateway {
    fun subscribe(
        userId: String,
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit = {}
    )
    fun unsubscribeAll()

}

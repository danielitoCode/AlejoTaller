package com.elitec.shared.sale.feature.sale.domain.realtime

import com.elitec.shared.core.feature.notifications.domain.entity.Promotion

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

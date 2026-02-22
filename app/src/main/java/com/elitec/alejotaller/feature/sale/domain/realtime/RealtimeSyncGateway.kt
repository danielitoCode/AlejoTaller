package com.elitec.alejotaller.feature.sale.domain.realtime

import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion

interface RealtimeSyncGateway {
    fun subscribe(
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit = {}
    )
    fun unsubscribeAll()

}
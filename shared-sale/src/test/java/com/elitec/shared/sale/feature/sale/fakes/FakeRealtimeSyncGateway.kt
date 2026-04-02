package com.elitec.shared.sale.feature.sale.fakes

import com.elitec.shared.core.feature.notifications.domain.entity.Promotion
import com.elitec.shared.sale.feature.sale.domain.realtime.RealtimeSyncGateway
import com.elitec.shared.sale.feature.sale.domain.realtime.SaleRealtimeEvent

class FakeRealtimeSyncGateway : RealtimeSyncGateway {
    var subscribedUserId: String? = null
    var unsubscribeCalls: Int = 0
    var connectCallback: (() -> Unit)? = null
    var disconnectCallback: (() -> Unit)? = null
    var saleEventCallback: ((SaleRealtimeEvent) -> Unit)? = null
    var promotionCallback: ((Promotion) -> Unit)? = null

    override fun subscribe(
        userId: String,
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit
    ) {
        subscribedUserId = userId
        connectCallback = onConnect
        disconnectCallback = onDisconnect
        saleEventCallback = onSaleEvent
        promotionCallback = onPromotion
    }

    override fun unsubscribeAll() {
        unsubscribeCalls += 1
    }
}

package com.elitec.alejotaller.infraestructure.core.data.realtime

import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeSyncGateway
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

class FakeRealTimeManager: RealtimeSyncGateway {

    override fun subscribe(
        userId: String,
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            eventLauncher(onConnect,onDisconnect,onSaleEvent,onPromotion)
        }
    }

    override fun unsubscribeAll() {}

    private suspend fun eventLauncher(
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit,
    ) {
        delay(1000)
        onConnect()
        delay(1500)
        onSaleEvent(SaleRealtimeEvent(
                saleId = "saleId 2",
                userId = "userId 2",
                isSuccess = true,
                cause = ""
            ))
        delay(2000)
        onPromotion(Promotion(
            id = "Promotion ID",
            title = "Promotion title test",
            message = "Promotion message test",
            imageUrl = "promotion url test",
            oldPrice = 11.0,
            currentPrice = 14.0,
            validFromEpochMillis = Clock.System.now().toEpochMilliseconds(),
            validUntilEpochMillis = Clock.System.now().toEpochMilliseconds()
        ))
        delay(1500)
        onDisconnect()
    }
}

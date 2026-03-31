package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.notifications.data.models.PromotionEvent
import com.elitec.alejotaller.feature.notifications.domain.caseuse.SavePromotionCaseUse
import com.elitec.shared.core.feature.notifications.domain.entity.Promotion
import com.elitec.shared.sale.feature.sale.domain.caseUse.SubscribeRealtimeSyncCaseUse
import com.elitec.alejotaller.infraestructure.core.data.realtime.RealTimeManagerImpl
import com.elitec.alejotaller.infraestructure.core.presentation.services.OrderNotificationService
import com.elitec.shared.sale.feature.sale.domain.caseUse.InterpretSaleRealtimeEventCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateSaleVerificationFromRealtimeCaseUse
import com.elitec.shared.sale.feature.sale.domain.realtime.RealtimeMessageKind
import com.elitec.shared.sale.feature.sale.domain.realtime.SaleRealtimeCommand
import com.elitec.shared.sale.feature.sale.domain.realtime.SaleRealtimeEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RealtimeSyncViewModel(
    private val subscribeRealtimeSyncCaseUse: SubscribeRealtimeSyncCaseUse,
    private val savePromotionCaseUse: SavePromotionCaseUse,
    private val orderNotificationService: OrderNotificationService,
    private val interpretSaleRealtimeEventCaseUse: InterpretSaleRealtimeEventCaseUse,
    private val updateSaleVerificationFromRealtimeCaseUse: UpdateSaleVerificationFromRealtimeCaseUse
) : ViewModel() {

    private companion object {
        const val TAG = "RealtimeSyncVM"
    }
    private var isSubscribed = false
    private var activeUserId: String = ""
    private var subscribedUserId: String = ""
    private val activePendingSaleIds = MutableStateFlow<Set<String>>(emptySet())
    private val _uiMessages = MutableSharedFlow<RealtimeUiMessage>(extraBufferCapacity = 16)
    val uiMessages = _uiMessages.asSharedFlow()

    fun startRealtimeSync() {
        if (activeUserId.isBlank()) return
        if (activePendingSaleIds.value.isEmpty()) return
        if (isSubscribed && subscribedUserId == activeUserId) return

        if (isSubscribed && subscribedUserId != activeUserId) {
            stopRealtimeSync()
        }

        subscribeRealtimeSyncCaseUse(
            userId = activeUserId,
            onConnect = { isSubscribed = true },
            onDisconnect = { isSubscribed = false },
            onSaleEvent = ::dispatchSaleEvent,
            onPromotion = ::persistPromotion
        )
        subscribedUserId = activeUserId
    }

    fun updateSubscriptionScope(userId: String, pendingSaleIds: Set<String>) {
        val previousUserId = activeUserId
        activeUserId = userId
        activePendingSaleIds.value = pendingSaleIds

        if (previousUserId.isNotBlank() && previousUserId != userId) {
            stopRealtimeSync()
        }
    }

    fun stopRealtimeSync() {
        if (!isSubscribed) {
            subscribedUserId = ""
            return
        }

        subscribeRealtimeSyncCaseUse.unsubscribeAll()
        isSubscribed = false
        subscribedUserId = ""
    }

    override fun onCleared() {
        stopRealtimeSync()
        super.onCleared()
    }

    private fun dispatchSaleEvent(event: SaleRealtimeEvent) {
        Log.i(TAG, "event=realtime_dispatch_received saleId=${event.saleId} userId=${event.userId} success=${event.isSuccess}")
        if (!shouldHandleSaleEvent(event, activeUserId, activePendingSaleIds.value)) {
            Log.i(TAG, "event=realtime_dispatch_ignored saleId=${event.saleId} activeUserId=$activeUserId")
            return
        }

        viewModelScope.launch {
            updateSaleVerificationFromRealtimeCaseUse(
                saleId = event.saleId,
                isSuccess = event.isSuccess
            ).onSuccess {
                Log.i(TAG, "event=realtime_status_persisted saleId=${event.saleId} nextState=${if (event.isSuccess) "VERIFIED" else "DELETED"}")
            }.onFailure { error ->
                Log.w(TAG, "event=realtime_status_persist_failed saleId=${event.saleId} cause=${error.message}", error)
            }
        }

        interpretSaleRealtimeEventCaseUse(event).forEach { command ->
            when (command) {
                is SaleRealtimeCommand.InAppMessage -> viewModelScope.launch {
                    Log.i(TAG, "event=realtime_in_app_message saleId=${event.saleId} kind=${command.kind}")
                    _uiMessages.emit(
                        RealtimeUiMessage(
                            message = command.message,
                            isError = command.kind == RealtimeMessageKind.Error
                        )
                    )
                }

                is SaleRealtimeCommand.PushNotification ->
                    orderNotificationService.showOrderStatus(
                        title = command.title,
                        message = command.body,
                        saleId = event.saleId
                    ).also {
                        Log.i(TAG, "event=realtime_push_notification saleId=${event.saleId} title=${command.title}")
                    }
            }
        }
    }

    private fun persistPromotion(promotion: Promotion) {
        viewModelScope.launch {
            savePromotionCaseUse(promotion)
        }
    }
}

data class RealtimeUiMessage(
    val message: String,
    val isError: Boolean
)

internal fun shouldHandleSaleEvent(
    event: SaleRealtimeEvent,
    activeUserId: String,
    activePendingSaleIds: Set<String>
): Boolean {
    if (activeUserId.isBlank()) return false
    if (event.userId.isBlank()) return false
    if (event.userId != activeUserId) return false
    return event.saleId in activePendingSaleIds
}

package com.elitec.alejotaller.infraestructure.core.data.realtime

class RealTimeManagerImpl(
    private val pusherManager: PusherManager
) {
    fun subscribeToABuyConfirmationChannel(
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onConfirmationEvent: (String) -> Unit,
        onConfirmationError: (String, String) -> Unit
    ) {
        pusherManager.init(onConnect, onDisconnect)
        pusherManager.subscribe(

        )
    }
}
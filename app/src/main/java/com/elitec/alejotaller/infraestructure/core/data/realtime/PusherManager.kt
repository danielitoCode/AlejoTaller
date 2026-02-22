package com.elitec.alejotaller.infraestructure.core.data.realtime

import android.util.Log
import com.pusher.client.Pusher
import com.pusher.client.channel.Channel
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PusherManager(
    private val pusher: Pusher
) {
    private var isConnected = false
    private val subscribedChannels = mutableMapOf<String, Channel>()

    fun init(onConnect: () -> Unit, onDisconnect: () -> Unit) {
        if (isConnected) {
            Log.i("PusherManager", "Pusher already initialized. Skipping connect().")
            return
        }

        Log.i("PusherManager", "Initializing Pusher…")

        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                when (change.currentState) {
                    ConnectionState.CONNECTED -> onConnect()
                    ConnectionState.DISCONNECTED,
                    ConnectionState.DISCONNECTING -> onDisconnect()
                    else -> Unit
                }
                Log.i(
                    "PusherManager",
                    "Connection state changed: ${change.previousState} -> ${change.currentState}"
                )
            }

            override fun onError(message: String, code: String, e: Exception) {
                onDisconnect()
                Log.e(
                    "PusherManager",
                    "Error connecting! Code: $code, Message: $message",
                    e
                )
            }
        }, ConnectionState.ALL)

        isConnected = true
    }

    fun subscribe(
        channel: String = "default",
        eventNames: List<String>,
        onReceive: ((RealtimeEventEnvelope) -> Unit)? = null
    ) {
        val normalizedChannel = channel.trim()

        if (normalizedChannel.isBlank()) {
            Log.e("PusherManager", "Cannot subscribe: channel is blank. Check PUSHER_*_CHANNEL config.")
            return
        }

        if (subscribedChannels.contains(normalizedChannel)) {
            Log.w("PusherManager", "Already subscribed to channel $normalizedChannel. Ignoring subscribe().")
            return
        }

        Log.i("PusherManager", "Subscribing to channel: $channel")

        val channelObj = pusher.subscribe(normalizedChannel)
        subscribedChannels[normalizedChannel] = channelObj

        eventNames.forEach { eventName ->
            channelObj.bind(eventName) { event ->
                val envelope = RealtimeEventEnvelope(
                    channel = normalizedChannel,
                    name = eventName,
                    payload = event.data
                )
                Log.i("PusherManager", "Received event '${envelope.name}' on '$normalizedChannel': ${event.data}")
                onReceive?.invoke(envelope)
            }
        }
    }

    fun unsubscribe(channel: String) {
        val normalizedChannel = channel.trim()
        if (normalizedChannel.isBlank()) return

        if (subscribedChannels.remove(normalizedChannel) != null) {
            pusher.unsubscribe(normalizedChannel)
            Log.i("PusherManager", "Unsubscribed from channel: $normalizedChannel")
        }
    }

    fun unsubscribeAll() {
        val channels = subscribedChannels.keys.toList()
        channels.forEach { channel ->
            pusher.unsubscribe(channel)
            Log.i("PusherManager", "Unsubscribed from channel: $channel")
        }
        subscribedChannels.clear()

        if (isConnected) {
            pusher.disconnect()
            isConnected = false
            Log.i("PusherManager", "Pusher disconnected after unsubscribeAll().")
        }
    }
}
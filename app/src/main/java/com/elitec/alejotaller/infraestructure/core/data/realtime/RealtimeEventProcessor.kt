package com.elitec.alejotaller.infraestructure.core.data.realtime

fun interface RealtimeEventProcessor {
    fun process(event: RealtimeEventEnvelope): Boolean
}
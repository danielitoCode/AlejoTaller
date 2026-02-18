package com.elitec.alejotaller.infraestructure.core.data.realtime

data class RealtimeEventEnvelope(
    val channel: String,
    val name: String,
    val payload: String?
)
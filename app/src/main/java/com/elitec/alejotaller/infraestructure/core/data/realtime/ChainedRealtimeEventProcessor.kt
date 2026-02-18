package com.elitec.alejotaller.infraestructure.core.data.realtime

abstract class ChainedRealtimeEventProcessor(
    private var next: RealtimeEventProcessor? = null
) : RealtimeEventProcessor {

    fun setNext(nextProcessor: RealtimeEventProcessor): RealtimeEventProcessor {
        next = nextProcessor
        return nextProcessor
    }

    final override fun process(event: RealtimeEventEnvelope): Boolean {
        val handled = handle(event)
        if (handled) return true
        return next?.process(event) ?: false
    }

    protected abstract fun handle(event: RealtimeEventEnvelope): Boolean
}
package com.elitec.alejotaller.feature.auth.domain.ports

/**
 * Persists whether the user already completed / skipped Welcome (Landing).
 * Aligns with web `alejo_has_visited`.
 */
interface FirstVisitStore {
    fun hasCompletedWelcome(): Boolean
    fun markWelcomeCompleted()
    fun clear()
}

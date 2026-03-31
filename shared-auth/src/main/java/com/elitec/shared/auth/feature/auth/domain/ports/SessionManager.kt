package com.elitec.shared.auth.feature.auth.domain.ports

interface SessionManager {
    suspend fun openEmailSession(email: String, password: String): String
    suspend fun isAnySessionAlive(): Boolean
    suspend fun closeCurrentSession()
}

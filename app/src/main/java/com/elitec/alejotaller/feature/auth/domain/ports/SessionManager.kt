package com.elitec.alejotaller.feature.auth.domain.ports

interface SessionManager {
    suspend fun openEmailSession(email: String, password: String)
    suspend fun isAnySessionAlive(): Boolean
    suspend fun closeCurrentSession()
}
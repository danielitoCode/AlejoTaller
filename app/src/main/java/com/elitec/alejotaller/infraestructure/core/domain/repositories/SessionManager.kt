package com.elitec.alejotaller.infraestructure.core.domain.repositories

interface SessionManager {
    suspend fun openEmailSession(email: String, password: String)
    suspend fun isAnySessionAlive(): Boolean
    suspend fun closeCurrentSession()
}
package com.elitec.alejotaller.feature.auth.domain.ports

interface SessionManager {
    suspend fun openEmailSession(email: String, password: String): String
    /** Creates an Appwrite anonymous session; returns userId. */
    suspend fun openAnonymousSession(): String
    suspend fun isAnySessionAlive(): Boolean
    suspend fun closeCurrentSession()
}

package com.elitec.alejotaller.infraestructure.core.data.repository

import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import io.appwrite.services.Account

class AppwriteSessionManager(
    private val account: Account
) : SessionManager {

    override suspend fun openEmailSession(email: String, password: String): String {
        return account.createEmailPasswordSession(email, password).userId
    }

    override suspend fun isAnySessionAlive(): Boolean {
        return runCatching {
            val sessions = account.listSessions()
            sessions.sessions.isNotEmpty()
        }.getOrDefault(false)
    }

    override suspend fun closeCurrentSession() {
        account.deleteSession("current")
    }
}
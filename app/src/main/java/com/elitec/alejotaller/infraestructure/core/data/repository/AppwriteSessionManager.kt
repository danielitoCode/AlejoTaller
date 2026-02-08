package com.elitec.alejotaller.infraestructure.core.data.repository

import com.elitec.alejotaller.infraestructure.core.domain.repositories.SessionManager
import io.appwrite.services.Account

class AppwriteSessionManager(
    private val account: Account
) : SessionManager {

    override suspend fun openEmailSession(email: String, password: String) {
        account.createEmailPasswordSession(email, password)
    }

    override suspend fun isAnySessionAlive(): Boolean {
        val sessions = account.listSessions()
        return sessions.sessions.isNotEmpty()
    }

    override suspend fun closeCurrentSession() {
        account.deleteSession("current")
    }
}
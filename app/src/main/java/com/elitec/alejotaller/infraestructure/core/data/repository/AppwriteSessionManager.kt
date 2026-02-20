package com.elitec.alejotaller.infraestructure.core.data.repository

import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account

class AppwriteSessionManager(
    private val account: Account
) : SessionManager {

    override suspend fun openEmailSession(email: String, password: String): String {
        return try {
            account.createEmailPasswordSession(email, password).userId
        } catch (e: Exception) {
            if (e !is AppwriteException) throw e

            val isActiveSessionError = e.message?.contains("session", ignoreCase = true) == true &&
                    (e.message?.contains("active", ignoreCase = true) == true ||
                            e.message?.contains("already", ignoreCase = true) == true)

            if (!isActiveSessionError) throw e

            runCatching { account.deleteSession("current") }
            account.createEmailPasswordSession(email, password).userId
        }
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
package com.elitec.alejotaller.fakes

import android.content.Context
import com.elitec.alejotaller.feature.auth.domain.entity.CustomFile
import com.elitec.alejotaller.feature.auth.domain.entity.GoogleUser
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository
import com.elitec.alejotaller.feature.auth.domain.repositories.FileRepository
import java.io.File

class FakeSessionManager(
    var openedSessionId: String = "session-1",
    var isAlive: Boolean = true,
    var openError: Throwable? = null,
    var closeError: Throwable? = null
) : SessionManager {
    val openCalls = mutableListOf<Pair<String, String>>()
    var closeCalls = 0

    override suspend fun openEmailSession(email: String, password: String): String {
        openCalls += email to password
        openError?.let { throw it }
        return openedSessionId
    }

    override suspend fun isAnySessionAlive(): Boolean = isAlive

    override suspend fun closeCurrentSession() {
        closeCalls += 1
        closeError?.let { throw it }
    }
}
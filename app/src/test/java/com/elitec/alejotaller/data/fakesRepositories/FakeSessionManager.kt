package com.elitec.alejotaller.data.fakesRepositories

import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager

class FakeSessionManager(
    var openedSessionId: String = "session-1",
    var isAlive: Boolean = true,
    var openError: Throwable? = null,
    var closeError: Throwable? = null,
    var anonymousError: Throwable? = null
) : SessionManager {
    val openCalls = mutableListOf<Pair<String, String>>()
    var closeCalls = 0
    var anonymousOpenCalls = 0

    override suspend fun openEmailSession(email: String, password: String): String {
        openCalls += email to password
        openError?.let { throw it }
        return openedSessionId
    }

    override suspend fun openAnonymousSession(): String {
        anonymousOpenCalls += 1
        anonymousError?.let { throw it }
        return openedSessionId
    }

    override suspend fun isAnySessionAlive(): Boolean = isAlive

    override suspend fun closeCurrentSession() {
        closeCalls += 1
        closeError?.let { throw it }
    }
}

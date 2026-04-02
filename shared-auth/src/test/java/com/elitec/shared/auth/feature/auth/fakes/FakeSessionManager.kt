package com.elitec.shared.auth.feature.auth.fakes

import com.elitec.shared.auth.feature.auth.domain.ports.SessionManager

class FakeSessionManager : SessionManager {
    var openedEmail: String? = null
    var openedPassword: String? = null
    var closeCalls: Int = 0

    override suspend fun openEmailSession(email: String, password: String): String {
        openedEmail = email
        openedPassword = password
        return "session-user-id"
    }

    override suspend fun isAnySessionAlive(): Boolean = true

    override suspend fun closeCurrentSession() {
        closeCalls += 1
    }
}

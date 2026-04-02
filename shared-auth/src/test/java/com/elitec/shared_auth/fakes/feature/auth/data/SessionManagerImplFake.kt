package com.elitec.shared_auth.fakes.feature.auth.data

import com.elitec.shared.auth.feature.auth.domain.ports.SessionManager

class SessionManagerImplFake: SessionManager {
    override suspend fun openEmailSession(
        email: String,
        password: String,
    ): String = "test user Id"

    override suspend fun isAnySessionAlive(): Boolean = true
    override suspend fun closeCurrentSession() {}
}
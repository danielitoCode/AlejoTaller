package com.elitec.shared.auth.feature.auth.domain.caseuse

import com.elitec.shared.auth.feature.auth.domain.ports.SessionManager

class AuthUserCaseUse(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String, pass: String): Result<String> = runCatching {
        sessionManager.openEmailSession(email.trim(), pass)
    }
}

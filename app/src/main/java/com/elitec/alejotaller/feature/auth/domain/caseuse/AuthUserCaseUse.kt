package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager

class AuthUserCaseUse(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String, pass: String): Result<String> = runCatching {
        sessionManager.openEmailSession(email,pass)
    }
}
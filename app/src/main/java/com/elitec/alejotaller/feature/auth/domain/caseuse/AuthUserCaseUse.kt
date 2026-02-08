package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.infraestructure.core.domain.repositories.SessionManager

class AuthUserCaseUse(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String, pass: String): Result<Unit> = runCatching {
        sessionManager.openEmailSession(email,pass)
    }
}
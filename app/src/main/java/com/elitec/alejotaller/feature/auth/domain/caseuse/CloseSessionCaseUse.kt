package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.infraestructure.core.domain.repositories.SessionManager

class CloseSessionCaseUse(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> =  runCatching {
        sessionManager.closeCurrentSession()
    }
}
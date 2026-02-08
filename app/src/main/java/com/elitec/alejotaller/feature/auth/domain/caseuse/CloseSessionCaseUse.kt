package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager

class CloseSessionCaseUse(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> =  runCatching {
        sessionManager.closeCurrentSession()
    }
}
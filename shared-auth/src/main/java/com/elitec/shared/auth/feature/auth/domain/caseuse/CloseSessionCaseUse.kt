package com.elitec.shared.auth.feature.auth.domain.caseuse

import com.elitec.shared.auth.feature.auth.domain.ports.SessionManager

class CloseSessionCaseUse(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        sessionManager.closeCurrentSession()
    }
}

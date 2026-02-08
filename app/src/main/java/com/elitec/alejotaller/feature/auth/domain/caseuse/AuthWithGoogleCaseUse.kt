package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.caseuse.util.hashEmailWithSub
import com.elitec.alejotaller.infraestructure.core.domain.repositories.GoogleAuthProvider
import com.elitec.alejotaller.infraestructure.core.domain.repositories.SessionManager

class AuthWithGoogleCaseUse(
    private val googleAuthProvider: GoogleAuthProvider,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val googleUser = googleAuthProvider.getUser()

        val password = hashEmailWithSub(
            email = googleUser.email,
            sub = googleUser.sub
        )

        sessionManager.openEmailSession(googleUser.email, password)
    }
}
package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.caseuse.util.hashEmailWithSub
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository
import io.appwrite.exceptions.AppwriteException

class AuthWithGoogleCaseUse(
    private val googleAuthProvider: GoogleAuthProvider,
    private val registerWithGoogleUseCase: RegisterWithGoogleUseCase,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val googleUser = googleAuthProvider.getUser()

        val password = hashEmailWithSub(
            email = googleUser.email,
            sub = googleUser.sub
        )

        try {
            sessionManager.openEmailSession(googleUser.email, password)
        } catch (e: Exception) {
            if(e is AppwriteException && e.code == 40) {
                registerWithGoogleUseCase()
            }
        }
    }
}
package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.caseuse.util.hashEmailWithSub
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager

class RegisterWithGoogleUseCase(
    private val googleAuthProvider: GoogleAuthProvider,
    private val sessionManager: SessionManager,
    private val accountRepository: AccountRepository
) {

    suspend operator fun invoke(): Result<String> = runCatching {

        val googleUser = googleAuthProvider.getUser()

        val password = hashEmailWithSub(
            email = googleUser.email,
            sub = googleUser.sub
        )

        val userProfile = UserProfile(
            sub = googleUser.sub,
            phone = googleUser.phone,
            photoUrl = googleUser.photoUrl,
            verification = false
        )

        accountRepository.create(
            User(
                id = "",
                name = googleUser.name,
                email = googleUser.email,
                pass = password,
                userProfile = userProfile
            )
        )

        sessionManager.openEmailSession(googleUser.email, password)

        accountRepository.updateProfile(userProfile)

        sessionManager.closeCurrentSession()

        googleUser.email
    }
}
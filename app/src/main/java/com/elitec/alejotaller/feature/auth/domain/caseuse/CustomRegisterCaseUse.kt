package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class CustomRegisterCaseUse(
    private val accountRepository: AccountRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<String> = runCatching {
        val user = User(
            id = "",
            name = name,
            email = email,
            pass = password,
            userProfile = UserProfile(
                sub = "",
                phone = "",
                photoUrl = "",
                verification = false
            )
        )
        accountRepository.create(user)
        sessionManager.openEmailSession(email,password)
    }
}


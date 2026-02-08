package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class CustomRegisterCaseUse(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<Unit> = runCatching {
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
    }
}


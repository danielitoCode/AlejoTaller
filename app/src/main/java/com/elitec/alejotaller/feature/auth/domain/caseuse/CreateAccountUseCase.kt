package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class CreateAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> = runCatching {
        when {
            user.email != null -> accountRepository.create(user)

            user.userProfile.phone != null -> accountRepository.create(user)

            else -> throw IllegalArgumentException("Datos inválidos")
        }
    }
}
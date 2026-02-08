package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class UpdateUserUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> =
        accountRepository.update(user)
}
package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class UpdateUserNameUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(newName: String): Result<Unit> = runCatching {
        accountRepository.updateName(newName)
    }
}
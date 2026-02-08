package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class VerifyUserUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        accountRepository.verifyEmail()
}
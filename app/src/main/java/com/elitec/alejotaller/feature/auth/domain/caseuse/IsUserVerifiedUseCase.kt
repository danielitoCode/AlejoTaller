package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class IsUserVerifiedUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): Result<Boolean> = runCatching {
        accountRepository.isVerified()
    }
}
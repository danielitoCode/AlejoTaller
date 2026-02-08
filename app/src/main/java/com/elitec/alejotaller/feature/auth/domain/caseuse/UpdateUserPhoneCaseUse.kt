package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class UpdateUserPhoneCaseUse (
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(newPhone: String): Result<Unit> = runCatching {
        accountRepository.updatePhone(newPhone)
    }
}
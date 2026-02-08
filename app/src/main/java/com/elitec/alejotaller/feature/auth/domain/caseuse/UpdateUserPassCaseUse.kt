package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class UpdateUserPassCaseUse(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(newPass: String): Result<Unit> = runCatching {
        accountRepository.updatePass(newPass)
    }
}
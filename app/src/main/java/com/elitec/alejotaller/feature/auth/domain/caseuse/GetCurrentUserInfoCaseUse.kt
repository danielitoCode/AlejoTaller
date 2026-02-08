package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class GetCurrentUserInfoCaseUse(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(): Result<User> = runCatching {
        accountRepository.getCurrentUserInfo()
    }
}
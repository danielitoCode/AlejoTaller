package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class UpdateUserPhotoUrlCaseUse(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(photoUrl: String): Result<Unit> = runCatching {
        accountRepository.updatePhotoUrl(photoUrl)
    }
}


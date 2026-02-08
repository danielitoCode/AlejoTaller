package com.elitec.alejotaller.feature.auth.domain.caseuse

import android.nfc.FormatException
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class CreateAccountUseCase(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> =
        when {
            user.email != null ->
                accountRepository.createWithEmail(user)

            user.phone != null ->
                accountRepository.createWithPhone(user)

            else ->
                Result.failure(FormatException("Datos inválidos"))
        }
}
package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository
import com.elitec.alejotaller.feature.auth.domain.repositories.FileRepository
import java.io.File

class UpdateUserPhotoUrlCaseUse(
    private val accountRepository: AccountRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(file: File, userId: String): Result<String> = runCatching {
        val response = fileRepository.uploadCompressedImage(file, userId)
        accountRepository.updatePhotoUrl(response.getOrThrow().viewUrl)
        response.getOrThrow().viewUrl
    }
}


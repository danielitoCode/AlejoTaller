package com.elitec.alejotaller.data.fakesRepositories

import com.elitec.alejotaller.feature.auth.domain.entity.CustomFile
import com.elitec.alejotaller.feature.auth.domain.repositories.FileRepository
import java.io.File

class FakeFileRepository(
    var result: Result<CustomFile> = Result.success(
        CustomFile(fileId = "file-1", viewUrl = "https://cdn/file-1")
    )
) : FileRepository {
    override suspend fun uploadCompressedImage(file: File, userId: String): Result<CustomFile> = result
}
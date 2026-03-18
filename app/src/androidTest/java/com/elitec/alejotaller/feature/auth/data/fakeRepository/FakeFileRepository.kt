package com.elitec.alejotaller.feature.auth.data.fakeRepository

import com.elitec.alejotaller.feature.auth.domain.entity.CustomFile
import com.elitec.alejotaller.feature.auth.domain.repositories.FileRepository
import java.io.File

class FakeFileRepository: FileRepository {
    override suspend fun uploadCompressedImage(
        file: File,
        userId: String,
    ): Result<CustomFile> {
        return Result.success(
            CustomFile(
                fileId = "file upload id",
                viewUrl = "url upload url"
            )
        )
    }
}
package com.elitec.alejotaller.feature.auth.domain.repositories

import com.elitec.alejotaller.feature.auth.domain.entity.CustomFile
import java.io.File

interface FileRepository {
    suspend fun uploadCompressedImage(file: File, userId: String): Result<CustomFile>
}
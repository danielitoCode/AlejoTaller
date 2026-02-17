package com.elitec.alejotaller.feature.auth.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.auth.data.dto.CustomFileDto
import com.elitec.alejotaller.feature.auth.domain.entity.CustomFile
import com.elitec.alejotaller.feature.auth.domain.repositories.FileRepository
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import io.appwrite.Permission
import io.appwrite.Role
import java.io.File

class FileUploadRepoImpl(
    private val storage: Storage
): FileRepository {
    suspend fun uploadImageFile(source: String, userId: String): Result<String> = runCatching {
        val fileId = ID.unique()
        storage.createFile(
            bucketId = BuildConfig.APPWRITE_BUCKECT_ID,
            fileId = fileId,
            file = InputFile.fromPath(source),
            permissions = listOf(
                Permission.read(Role.any()),  // 👈 público
                Permission.write(Role.user(userId))
            )
        )

        val result = storage.getFileView(
            bucketId = BuildConfig.APPWRITE_BUCKECT_ID,
            fileId = fileId
        )

        result.toString()
    }

    override suspend fun uploadCompressedImage(file: File, userId: String): Result<CustomFile> = runCatching {
        // Subida a Appwrite
        val fileId = ID.unique()
        val uploadedFile  = storage.createFile(
            bucketId = BuildConfig.APPWRITE_BUCKECT_ID,
            fileId = fileId,
            file = InputFile.fromFile(file),
            permissions = listOf(
                Permission.read(Role.any()),  // 👈 público
                Permission.write(Role.user(userId))
            )
        )

        // Obtener URL pública
        val viewUrl = storage.getFileView(
            bucketId = BuildConfig.APPWRITE_BUCKECT_ID,
            fileId = uploadedFile.id
        )

        CustomFile(fileId = uploadedFile.id, viewUrl = viewUrl.toString())
    }
}
package com.elitec.alejotaller.feature.auth.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CustomFileDto(
    val fileId: String,
    val viewUrl: String
)

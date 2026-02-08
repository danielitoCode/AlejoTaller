package com.elitec.alejotaller.feature.category.data.mapper

import com.elitec.alejotaller.feature.category.data.dto.CategoryDto

fun CategoryDto.toDocument(): Map<String, Any?> =
    mapOf(
        "id" to id,
        "name" to name,
        "photo_url" to photoUrl
    )
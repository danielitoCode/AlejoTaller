package com.elitec.alejotaller.feature.category.data.mapper

import com.elitec.alejotaller.feature.category.domain.entity.Category
import io.appwrite.models.Document

fun Category.toDocument(): Map<String, Any?> =
    mapOf(
        "id" to id,
        "name" to name,
        "photoUrl" to photoUrl
    )
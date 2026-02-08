package com.elitec.alejotaller.feature.category.data.mapper

import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import io.appwrite.models.Document

fun  Document<Map<String, Any>>.toCategoryDto(): CategoryDto =
    CategoryDto(
        id = this.id, // Manejar null con valor por defecto
        name = (data["name"] as? String) ?: "Sin nombre",
        photoUrl = (data["photo_url"] as? String) ?: ""
    )
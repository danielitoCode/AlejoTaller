package com.elitec.alejotaller.feature.category.data.mapper

import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.category.domain.entity.Category

fun Category.toDto(): CategoryDto =
    CategoryDto(
        id = this.id,
        name = this.name,
        photoUrl = this.photoUrl
    )
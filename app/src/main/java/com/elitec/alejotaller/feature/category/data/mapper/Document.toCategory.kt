package com.elitec.alejotaller.feature.category.data.mapper

import com.elitec.alejotaller.feature.category.domain.entity.Category
import io.appwrite.models.Document

fun Document<Category>.toCategory(): Category =
    Category(
        id = this.id,
        name = this.data.id,
        photoUrl = this.data.photoUrl
    )
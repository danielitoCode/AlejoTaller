package com.elitec.alejotaller.feature.category.domain.entity

import com.elitec.alejotaller.infraestructure.core.domain.CoreEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category (
    override val id: String,
    val name: String,
    @SerialName("photo_url")
    val photoUrl: String?
): CoreEntity
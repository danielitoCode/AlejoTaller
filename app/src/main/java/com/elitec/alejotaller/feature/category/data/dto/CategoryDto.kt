package com.elitec.alejotaller.feature.category.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CategoryDto(
    @PrimaryKey val id: String,
    val name: String,
    @SerialName("photo_url")
    val photoUrl: String?
)
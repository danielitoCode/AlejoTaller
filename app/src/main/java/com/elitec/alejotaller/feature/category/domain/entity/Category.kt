package com.elitec.alejotaller.feature.category.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elitec.alejotaller.infraestructure.core.domain.CoreEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Category (
    @PrimaryKey override val id: String,
    val name: String,
    @SerialName("photo_url")
    val photoUrl: String?
): CoreEntity
package com.elitec.alejotaller.feature.auth.data.dto

import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import kotlinx.serialization.Serializable

@Serializable
data class UserDto (
    val id: String,
    val name: String,
    val email: String,
    val pass: String,
    val sub: String,
    val phone: String? = "",
    val photoUrl: String? = "",
    val verification: Boolean = false
)
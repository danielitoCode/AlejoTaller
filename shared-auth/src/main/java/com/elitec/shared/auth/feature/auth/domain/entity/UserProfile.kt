package com.elitec.shared.auth.feature.auth.domain.entity

data class UserProfile(
    val sub: String,
    val phone: String? = "",
    val photoUrl: String? = "",
    val verification: Boolean = false,
    val role: String? = null
)

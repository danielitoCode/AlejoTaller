package com.elitec.alejotaller.feature.auth.domain.entity

data class UserProfile(
    val sub: String,
    val phone: String? = "",
    val photoUrl: String? = "",
    val verification: Boolean = false
)
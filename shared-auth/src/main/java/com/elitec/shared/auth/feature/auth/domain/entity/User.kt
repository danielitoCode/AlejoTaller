package com.elitec.shared.auth.feature.auth.domain.entity

data class User(
    val id: String,
    val name: String,
    val email: String,
    val pass: String,
    val userProfile: UserProfile
) {
    init {
        require(name.isNotBlank()) { "El nombre no puede estar vacio" }
    }
}

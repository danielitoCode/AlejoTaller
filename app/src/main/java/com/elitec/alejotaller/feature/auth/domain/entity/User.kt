package com.elitec.alejotaller.feature.auth.domain.entity

data class User(
    val id: String,
    val name: String,
    val email: String,
    val pass: String,
    val userProfile: UserProfile
) {
    init {
        // AUTH_POLICY: visitors may have empty email/name (anonymous Appwrite).
        // Authenticated profiles still require a non-blank name.
        if (email.isNotBlank()) {
            require(name.isNotBlank()) { "El nombre no puede estar vacío" }
        }
    }

    val displayName: String
        get() = when {
            name.isNotBlank() -> name
            email.isBlank() -> "Visitante"
            else -> email
        }
}

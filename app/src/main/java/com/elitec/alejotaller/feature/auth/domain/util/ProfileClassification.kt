package com.elitec.alejotaller.feature.auth.domain.util

import com.elitec.alejotaller.feature.auth.domain.entity.User

/**
 * Mirrors web AUTH_POLICY / profile-classification.ts
 * If the profile is not clearly authenticated → visitor.
 */
enum class SessionMode {
    AUTHENTICATED,
    VISITOR
}

private val GUEST_PROVIDERS = setOf("guest", "anonymous", "visitor", "invitado")

fun isGuestProvider(provider: String?): Boolean {
    if (provider.isNullOrBlank()) return false
    return provider.trim().lowercase() in GUEST_PROVIDERS
}

fun isAnonymousProfile(email: String?, provider: String? = null): Boolean {
    if (isGuestProvider(provider)) return true
    return email.isNullOrBlank()
}

fun hasClearAuthenticatedProfile(
    id: String?,
    email: String?,
    provider: String? = null
): Boolean {
    if (id.isNullOrBlank()) return false
    if (isGuestProvider(provider)) return false
    if (isAnonymousProfile(email, provider)) return false
    return !email.isNullOrBlank()
}

fun hasClearAuthenticatedProfile(user: User?): Boolean {
    if (user == null) return false
    return hasClearAuthenticatedProfile(id = user.id, email = user.email, provider = null)
}

fun classifySessionMode(user: User?): SessionMode {
    return if (hasClearAuthenticatedProfile(user)) SessionMode.AUTHENTICATED else SessionMode.VISITOR
}

fun classifySessionMode(id: String?, email: String?, provider: String? = null): SessionMode {
    return if (hasClearAuthenticatedProfile(id, email, provider)) {
        SessionMode.AUTHENTICATED
    } else {
        SessionMode.VISITOR
    }
}

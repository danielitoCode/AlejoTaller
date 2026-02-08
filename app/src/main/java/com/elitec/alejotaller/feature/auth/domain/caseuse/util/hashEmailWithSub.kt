package com.elitec.alejotaller.feature.auth.domain.caseuse.util

import java.security.MessageDigest

fun hashEmailWithSub(email: String, sub: String): String {
    val input = "$email:$sub" // Combina email + sub con un separador
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
    // Convertimos los bytes a una cadena hexadecimal legible
    return hashBytes.joinToString("") { "%02x".format(it) }
}
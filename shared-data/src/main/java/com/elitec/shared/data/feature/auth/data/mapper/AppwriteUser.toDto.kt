package com.elitec.shared.data.feature.auth.data.mapper

import com.elitec.shared.data.feature.auth.data.dto.UserDto
import io.appwrite.models.User

fun User<Map<String, Any>>.toDto(): UserDto =
    let { currentUser ->
        val labels = currentUser.labels.map { it.trim().lowercase() }
        val roleFromLabels =
            when {
                "owner" in labels -> "owner"
                "administrator" in labels -> "administrator"
                "admin" in labels -> "admin"
                "operator" in labels -> "operator"
                else -> null
            }
        UserDto(
            id = currentUser.id,
            name = currentUser.name,
            email = currentUser.email,
            pass = currentUser.password ?: "",
            sub = (currentUser.prefs.data["sub"] as? String) ?: "",
            phone = (currentUser.prefs.data["phone"] as? String) ?: "",
            photoUrl = (currentUser.prefs.data["photoUrl"] as? String)
                ?: (currentUser.prefs.data["photo_url"] as? String)
                ?: (currentUser.prefs.data["avatarUrl"] as? String)
                ?: "",
            verification = (currentUser.prefs.data["verification"] as? Boolean) ?: false,
            role = ((currentUser.prefs.data["role"] as? String)?.trim()).takeUnless { it.isNullOrEmpty() }
                ?: roleFromLabels
        )
    }

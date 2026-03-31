package com.elitec.shared.data.feature.auth.data.mapper

import com.elitec.shared.data.feature.auth.data.dto.UserDto
import io.appwrite.models.User

fun User<Map<String, Any>>.toDto(): UserDto =
    UserDto(
        id = id,
        name = name,
        email = email,
        pass = password ?: "",
        sub = (prefs.data["sub"] as? String) ?: "",
        phone = (prefs.data["phone"] as? String) ?: "",
        photoUrl = (prefs.data["photoUrl"] as? String)
            ?: (prefs.data["photo_url"] as? String)
            ?: (prefs.data["avatarUrl"] as? String)
            ?: "",
        verification = (prefs.data["verification"] as? Boolean) ?: false,
        role = (prefs.data["role"] as? String)?.trim()
    )

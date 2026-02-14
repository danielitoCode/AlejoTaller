package com.elitec.alejotaller.feature.auth.data.mappers

import com.elitec.alejotaller.feature.auth.data.dto.UserDto
import io.appwrite.models.User

fun User<Map<String, Any>>.toDto(): UserDto =
    UserDto(
        id = id,
        name = name,
        email = email,
        pass = password ?: "",
        sub = (prefs.data["sub"] as? String) ?: "",
        phone = (prefs.data["sub"] as? String) ?: "",
        photoUrl = (prefs.data["sub"] as? String) ?: "",
        verification = (prefs.data["verification"] as? Boolean) ?: false
    )
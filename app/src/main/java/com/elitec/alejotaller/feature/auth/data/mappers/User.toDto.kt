package com.elitec.alejotaller.feature.auth.data.mappers

import com.elitec.alejotaller.feature.auth.data.dto.UserDto
import com.elitec.alejotaller.feature.auth.domain.entity.User

fun User.toDto(): UserDto =
    UserDto(
        id = id,
        name = name,
        email = email,
        pass = pass,
        sub = userProfile.sub,
        phone = userProfile.phone,
        photoUrl = userProfile.photoUrl,
        verification = userProfile.verification
    )
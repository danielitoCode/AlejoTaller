package com.elitec.alejotaller.feature.auth.data.mappers

import com.elitec.alejotaller.feature.auth.data.dto.UserDto
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile

fun UserDto.toDomain(): User {
    val profile = UserProfile(
        sub = sub,
        phone = phone,
        photoUrl = photoUrl,
        verification = verification
    )
    return User(
        id = id,
        name = name,
        email = email,
        pass = pass,
        userProfile = profile
    )
}
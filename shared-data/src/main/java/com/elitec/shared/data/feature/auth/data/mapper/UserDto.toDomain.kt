package com.elitec.shared.data.feature.auth.data.mapper

import com.elitec.shared.auth.feature.auth.domain.entity.User
import com.elitec.shared.auth.feature.auth.domain.entity.UserProfile
import com.elitec.shared.data.feature.auth.data.dto.UserDto

fun UserDto.toDomain(): User {
    val profile = UserProfile(
        sub = sub,
        phone = phone,
        photoUrl = photoUrl,
        verification = verification,
        role = role
    )
    return User(
        id = id,
        name = name,
        email = email,
        pass = pass,
        userProfile = profile
    )
}

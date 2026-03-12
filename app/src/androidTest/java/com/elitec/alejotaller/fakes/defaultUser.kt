package com.elitec.alejotaller.fakes

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile

fun defaultUser() = User(
    id = "user-1",
    name = "Alejo",
    email = "alejo@elitec.dev",
    pass = "123456",
    userProfile = UserProfile(
        sub = "sub-1",
        phone = "+5355512345",
        photoUrl = "https://img",
        verification = false
    )
)
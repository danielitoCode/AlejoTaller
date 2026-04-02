package com.elitec.shared_auth.fakes.feature.auth.data

import com.elitec.shared.auth.feature.auth.domain.entity.User
import com.elitec.shared.auth.feature.auth.domain.entity.UserProfile
import com.elitec.shared.auth.feature.auth.domain.repositories.AccountRepository

class AccountRepositoryImplFake(
    private val userId: String? = "user test Id",
    private val verified: Boolean,
    private val role: String
): AccountRepository {
    override suspend fun getCurrentUserInfo(): User =
        User(
            id = "user test Id",
            name ="Test user name",
            email = "email@test.domain",
            pass = "test user passkey",
            userProfile = UserProfile(
                sub = "sub user google account test",
                phone = "+55555555",
                photoUrl = "photo url user test",
                verification = verified,
                role = role
            )
        )
}
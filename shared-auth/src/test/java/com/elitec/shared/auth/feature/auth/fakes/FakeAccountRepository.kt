package com.elitec.shared.auth.feature.auth.fakes

import com.elitec.shared.auth.feature.auth.domain.entity.User
import com.elitec.shared.auth.feature.auth.domain.entity.UserProfile
import com.elitec.shared.auth.feature.auth.domain.repositories.AccountRepository

class FakeAccountRepository(
    private val role: String? = "viewer",
    private val userId: String = "user-test-id"
) : AccountRepository {
    override suspend fun getCurrentUserInfo(): User =
        User(
            id = userId,
            name = "Test User",
            email = "test@alejo.dev",
            pass = "secret",
            userProfile = UserProfile(
                sub = "google-sub",
                phone = "+5350000000",
                photoUrl = "",
                verification = true,
                role = role
            )
        )
}

package com.elitec.alejotaller.feature.auth.data.fakeRepository

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class FakeAccountRepository: AccountRepository {
    private val userTest = User(
        id = "testId",
        name = "Test User Name",
        email = "test.user.email@example.com",
        pass = "pass",
        userProfile = UserProfile(
            sub = "googleSubTest",
            phone = "+55003300",
            photoUrl = "photo url test",
            verification = false
        )
    )
    override suspend fun create(user: User) {}
    override suspend fun updateProfile(userProfile: UserProfile) {}
    override suspend fun updateName(newName: String) {}
    override suspend fun updatePass(newPass: String) {}
    override suspend fun updatePhone(newPhone: String) {}
    override suspend fun updatePhotoUrl(photoUrl: String) {}
    override suspend fun getCurrentUserInfo(): User = userTest
    override suspend fun verifyEmail() {}
    override suspend fun isVerified(): Boolean = userTest.userProfile.verification
}
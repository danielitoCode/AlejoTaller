package com.elitec.alejotaller.fakes

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository

class FakeAccountRepository(
    var currentUser: User = defaultUser(),
    var getCurrentUserError: Throwable? = null
) : AccountRepository {
    val createdUsers = mutableListOf<User>()
    val updatedProfiles = mutableListOf<UserProfile>()
    val updatedNames = mutableListOf<String>()
    val updatedPasses = mutableListOf<String>()
    val updatedPhones = mutableListOf<String>()
    val updatedPhotoUrls = mutableListOf<String>()

    override suspend fun create(user: User) {
        createdUsers += user
    }

    override suspend fun updateProfile(userProfile: UserProfile) {
        updatedProfiles += userProfile
    }

    override suspend fun updateName(newName: String) {
        updatedNames += newName
    }

    override suspend fun updatePass(newPass: String) {
        updatedPasses += newPass
    }

    override suspend fun updatePhone(newPhone: String) {
        updatedPhones += newPhone
    }

    override suspend fun updatePhotoUrl(photoUrl: String) {
        updatedPhotoUrls += photoUrl
    }

    override suspend fun getCurrentUserInfo(): User {
        getCurrentUserError?.let { throw it }
        return currentUser
    }

    override suspend fun verifyEmail() = Unit

    override suspend fun isVerified(): Boolean = currentUser.userProfile.verification
}

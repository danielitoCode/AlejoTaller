package com.elitec.alejotaller.feature.auth.data

import com.elitec.alejotaller.feature.auth.data.mappers.toDomain
import com.elitec.alejotaller.feature.auth.data.mappers.toDto
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository
import io.appwrite.ID
import io.appwrite.services.Account

class AccountRepositoryImpl(
    private val account: Account
): AccountRepository {
    override suspend fun create(user: User)  {
        val dto = user.toDto()
        account.create(
            userId = ID.unique(),
            email = dto.email,
            password = dto.pass,
            name = dto.name
        )
    }

    override suspend fun updateName(newName: String) {
        account.updateName(newName)
    }

    override suspend fun updatePass(newPass: String) {
        account.updatePassword(newPass)
    }

    override suspend fun updateProfile(profile: UserProfile) {
        account.updatePrefs(
            prefs = mapOf(
                "photoUrl" to profile.photoUrl,
                "sub" to profile.sub,
                "phone" to profile.phone,
                "verification" to profile.verification
            )
        )
    }

    override suspend fun getCurrentUserInfo(): User {
        val currentUser = account.get().toDto()
        return currentUser.toDomain()
    }

    override suspend fun verifyEmail() {
        account.updatePrefs(
            prefs = mapOf("verification" to true)
        )
    }

    override suspend fun isVerified(): Boolean {
        val prefs = account.getPrefs()
        return prefs.data["verification"] as? Boolean ?: false
    }
}
package com.elitec.alejotaller.feature.auth.data.repository

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
            userId = ID.Companion.unique(),
            email = dto.email,
            password = dto.pass,
            name = dto.name
        )
    }

    override suspend fun updateProfile(userProfile: UserProfile) {
        account.updatePrefs(
            prefs = mapOf(
                "sub" to userProfile.sub,
                "phone" to userProfile.phone,
                "photoUrl" to userProfile.photoUrl,
                "verification" to userProfile.verification
            )
        )
    }

    override suspend fun updateName(newName: String) {
        account.updateName(newName)
    }

    override suspend fun updatePass(newPass: String) {
        account.updatePassword(newPass)
    }

    override suspend fun updatePhone(newPhone: String) {
        account.updatePrefs(
            prefs = mapOf("phone" to newPhone)
        )
    }

    override suspend fun updatePhotoUrl(photoUrl: String) {
        account.updatePrefs(
            prefs = mapOf("photoUrl" to photoUrl)
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
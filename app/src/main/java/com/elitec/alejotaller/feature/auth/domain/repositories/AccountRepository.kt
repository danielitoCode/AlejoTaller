package com.elitec.alejotaller.feature.auth.domain.repositories

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile

interface AccountRepository {
    suspend fun create(user: User)
    suspend fun updateName(newName: String)
    suspend fun updatePass(newPass: String)
    suspend fun updateProfile(profile: UserProfile)
    suspend fun getCurrentUserInfo(): User
    suspend fun verifyEmail()
    suspend fun isVerified(): Boolean
}
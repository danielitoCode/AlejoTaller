package com.elitec.shared.data.feature.auth.data.repository

import com.elitec.shared.auth.feature.auth.domain.entity.User
import com.elitec.shared.auth.feature.auth.domain.repositories.AccountRepository
import com.elitec.shared.data.feature.auth.data.mapper.toDomain
import com.elitec.shared.data.feature.auth.data.mapper.toDto
import io.appwrite.services.Account

class AccountRepositoryImpl(
    private val account: Account
) : AccountRepository {
    override suspend fun getCurrentUserInfo(): User {
        val currentUser = account.get().toDto()
        return currentUser.toDomain()
    }
}

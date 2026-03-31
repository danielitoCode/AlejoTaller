package com.elitec.shared.auth.feature.auth.domain.repositories

import com.elitec.shared.auth.feature.auth.domain.entity.User

interface AccountRepository {
    suspend fun getCurrentUserInfo(): User
}

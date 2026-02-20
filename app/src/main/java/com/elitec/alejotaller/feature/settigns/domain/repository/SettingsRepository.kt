package com.elitec.alejotaller.feature.settigns.domain.repository

import com.elitec.alejotaller.feature.settigns.domain.entity.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun updateDarkMode(enabled: Boolean)
    suspend fun updateNotifications(enabled: Boolean)
    suspend fun updateHapticFeedback(enabled: Boolean)
}
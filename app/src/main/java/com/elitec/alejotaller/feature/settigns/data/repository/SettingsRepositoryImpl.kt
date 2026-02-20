package com.elitec.alejotaller.feature.settigns.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.elitec.alejotaller.feature.settigns.domain.entity.AppSettings
import com.elitec.alejotaller.feature.settigns.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun observeSettings(): Flow<AppSettings> {
        return dataStore.data.map { preferences ->
            AppSettings(
                darkMode = preferences[KEY_DARK_MODE] ?: false,
                notificationsEnabled = preferences[KEY_NOTIFICATIONS] ?: true,
                hapticFeedbackEnabled = preferences[KEY_HAPTIC] ?: false
            )
        }
    }

    override suspend fun updateDarkMode(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    override suspend fun updateNotifications(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    override suspend fun updateHapticFeedback(enabled: Boolean) {
        dataStore.edit { it[KEY_HAPTIC] = enabled }
    }

    private companion object {
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val KEY_HAPTIC = booleanPreferencesKey("haptic_feedback_enabled")
    }
}
package com.elitec.alejotaller.data.fakesRepositories

import com.elitec.alejotaller.feature.settigns.domain.entity.AppSettings
import com.elitec.alejotaller.feature.settigns.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {
    private val flow = MutableStateFlow(initial)
    var darkModeUpdatedTo: Boolean? = null
    var notificationsUpdatedTo: Boolean? = null
    var hapticUpdatedTo: Boolean? = null

    override fun observeSettings(): Flow<AppSettings> = flow

    override suspend fun updateDarkMode(enabled: Boolean) {
        darkModeUpdatedTo = enabled
    }

    override suspend fun updateNotifications(enabled: Boolean) {
        notificationsUpdatedTo = enabled
    }

    override suspend fun updateHapticFeedback(enabled: Boolean) {
        hapticUpdatedTo = enabled
    }
}
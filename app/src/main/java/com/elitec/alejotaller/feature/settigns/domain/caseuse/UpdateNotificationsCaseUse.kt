package com.elitec.alejotaller.feature.settigns.domain.caseuse

import com.elitec.alejotaller.feature.settigns.domain.repository.SettingsRepository

class UpdateNotificationsCaseUse(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.updateNotifications(enabled)
    }
}
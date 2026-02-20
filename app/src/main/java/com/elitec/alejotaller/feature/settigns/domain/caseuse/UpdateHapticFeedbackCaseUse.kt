package com.elitec.alejotaller.feature.settigns.domain.caseuse

import com.elitec.alejotaller.feature.settigns.domain.repository.SettingsRepository

class UpdateHapticFeedbackCaseUse(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.updateHapticFeedback(enabled)
    }
}
package com.elitec.alejotaller.feature.settigns.domain.caseuse

import com.elitec.alejotaller.feature.settigns.domain.repository.SettingsRepository

class ObserveSettingsCaseUse(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke() = settingsRepository.observeSettings()
}
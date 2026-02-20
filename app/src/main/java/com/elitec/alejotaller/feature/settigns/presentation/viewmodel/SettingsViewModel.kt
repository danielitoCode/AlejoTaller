package com.elitec.alejotaller.feature.settigns.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.settigns.domain.caseuse.ObserveSettingsCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateDarkModeCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateHapticFeedbackCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateNotificationsCaseUse
import com.elitec.alejotaller.feature.settigns.domain.entity.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeSettingsCaseUse: ObserveSettingsCaseUse,
    private val updateDarkModeCaseUse: UpdateDarkModeCaseUse,
    private val updateNotificationsCaseUse: UpdateNotificationsCaseUse,
    private val updateHapticFeedbackCaseUse: UpdateHapticFeedbackCaseUse
) : ViewModel() {

    val settings = observeSettingsCaseUse()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            updateDarkModeCaseUse(enabled)
        }
    }

    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            updateNotificationsCaseUse(enabled)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            updateHapticFeedbackCaseUse(enabled)
        }
    }
}
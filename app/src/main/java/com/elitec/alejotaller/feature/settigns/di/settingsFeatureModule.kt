package com.elitec.alejotaller.feature.settigns.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.elitec.alejotaller.feature.settigns.data.repository.SettingsRepositoryImpl
import com.elitec.alejotaller.feature.settigns.domain.caseuse.ObserveSettingsCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateDarkModeCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateHapticFeedbackCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateNotificationsCaseUse
import com.elitec.alejotaller.feature.settigns.domain.repository.SettingsRepository
import com.elitec.alejotaller.feature.settigns.presentation.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.coroutines.EmptyCoroutineContext.get

private const val SETTINGS_DATASTORE = "app_settings.preferences_pb"

val settingsFeatureModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create(
            produceFile = { androidContext().preferencesDataStoreFile(SETTINGS_DATASTORE) }
        )
    }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    factory { ObserveSettingsCaseUse(get()) }
    factory { UpdateDarkModeCaseUse(get()) }
    factory { UpdateNotificationsCaseUse(get()) }
    factory { UpdateHapticFeedbackCaseUse(get()) }

    viewModel { SettingsViewModel(get(), get(), get(), get()) }
}
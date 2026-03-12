package com.elitec.alejotaller.feature.product.domain.caseuse

import com.elitec.alejotaller.data.fakesRepositories.FakeSettingsRepository
import com.elitec.alejotaller.feature.settigns.domain.caseuse.ObserveSettingsCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateDarkModeCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateHapticFeedbackCaseUse
import com.elitec.alejotaller.feature.settigns.domain.caseuse.UpdateNotificationsCaseUse
import com.elitec.alejotaller.feature.settigns.domain.entity.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SettingsCaseUsesTest {
    @Test
    fun `observe settings delegates to repository stream`() = runTest {
        val repo = FakeSettingsRepository(
            initial = AppSettings (
                darkMode = true,
                notificationsEnabled = false,
                hapticFeedbackEnabled = true
            )
        )
        val caseUse = ObserveSettingsCaseUse(repo)

        val result = caseUse().first()

        assertEquals(AppSettings(true, false, true), result)
    }

    @Test
    fun `update dark mode delegates to repository`() = runTest {
        val repo = FakeSettingsRepository()
        val caseUse = UpdateDarkModeCaseUse(repo)

        caseUse(true)

        assertEquals(true, repo.darkModeUpdatedTo)
    }

    @Test
    fun `update notifications delegates to repository`() = runTest {
        val repo = FakeSettingsRepository()
        val caseUse = UpdateNotificationsCaseUse(repo)

        caseUse(false)

        assertEquals(false, repo.notificationsUpdatedTo)
    }

    @Test
    fun `update haptic feedback delegates to repository`() = runTest {
        val repo = FakeSettingsRepository()
        val caseUse = UpdateHapticFeedbackCaseUse(repo)

        caseUse(true)

        assertEquals(true, repo.hapticUpdatedTo)
    }
}
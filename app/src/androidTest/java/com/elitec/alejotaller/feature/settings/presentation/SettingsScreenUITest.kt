package com.elitec.alejotaller.feature.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elitec.alejotaller.feature.sale.presentation.screen.BuyReservationDetailsScreen
import com.elitec.alejotaller.feature.sale.presentation.screen.DELIVERY_SECTION_TAG
import com.elitec.alejotaller.feature.settigns.presentation.screen.SettingsScreen
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dark_mode_activation() = runTest {
        composeTestRule.setContent {
            AlejoTallerTheme {
                SettingsScreen()
            }
        }

        delay(1000)

        // Verificar que la sección de entrega es visible
        composeTestRule.onNodeWithText("Tema oscuro").assertIsDisplayed()
    }
}
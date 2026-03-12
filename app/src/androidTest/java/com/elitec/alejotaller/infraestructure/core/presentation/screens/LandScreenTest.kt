package com.elitec.alejotaller.infraestructure.core.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elitec.alejotaller.infraestructure.core.presentation.MainActivity
import com.elitec.alejotaller.infraestructure.core.presentation.components.NAVIGATE_TO_LOGIN_BUTTON
import com.elitec.alejotaller.infraestructure.core.presentation.components.NAVIGATE_TO_REGISTER_BUTTON
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// UI Test
@RunWith(AndroidJUnit4::class)
class LandScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun navigate_to_login_test() {
        var signInClicks = 0

        composeRule.setContent {
            LandScreen(onSignInClick = { signInClicks++ })
        }

        composeRule.onNodeWithTag(NAVIGATE_TO_LOGIN_BUTTON)
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, signInClicks)
    }

    @Test
    fun navigate_to_register_test() {
        var signUpClicks = 0

        composeRule.setContent {
            LandScreen(onSignUpClick = { signUpClicks++ })
        }

        composeRule.onNodeWithTag(NAVIGATE_TO_REGISTER_BUTTON)
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, signUpClicks)
    }
}
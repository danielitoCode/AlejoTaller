package com.elitec.alejotaller.infraestructure.core.presentation.screens

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elitec.alejotaller.infraestructure.core.presentation.MainActivity
import com.elitec.alejotaller.infraestructure.core.presentation.components.NAVIGATE_TO_LOGIN_BUTTON
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// UI Test
@RunWith(AndroidJUnit4::class)
class LandScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigate_to_login_test() {

    }
}
package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elitec.alejotaller.feature.auth.di.authFeatureDiModule
import com.elitec.alejotaller.feature.auth.di.authTestDiModule
import com.elitec.alejotaller.feature.category.di.category.test.categoryTestDiModule
import com.elitec.alejotaller.feature.notifications.di.notificationTestDiModule
import com.elitec.alejotaller.feature.product.di.productTestDiModule
import com.elitec.alejotaller.feature.sale.di.saleTestDiModule
import com.elitec.alejotaller.infraestructure.core.di.infrastructureTestDiModule
import com.elitec.alejotaller.infraestructure.core.presentation.screens.LandScreen
import com.elitec.alejotaller.infraestructure.di.infrastructureModule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule

@RunWith(AndroidJUnit4::class)
class InternalNavigationWrapperTest : KoinTest {
    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(ApplicationProvider.getApplicationContext())
        modules(
            infrastructureTestDiModule,
            authTestDiModule,
            categoryTestDiModule,
            productTestDiModule,
            notificationTestDiModule,
            saleTestDiModule
        )
    }

    @Test
    fun internalNavigationWrapperTest() {
        composeRule.setContent {
            InternalNavigationWrapper(
                onNavigateBack = {},
                userId = "user test 1",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
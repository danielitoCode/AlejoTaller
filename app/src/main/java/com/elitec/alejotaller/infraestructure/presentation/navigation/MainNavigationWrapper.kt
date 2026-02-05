package com.elitec.alejotaller.infraestructure.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.elitec.alejotaller.infraestructure.extention.presentation.navigateBack
import com.elitec.alejotaller.infraestructure.extention.presentation.navigateTo
import com.elitec.alejotaller.infraestructure.presentation.screens.DetailScreen
import com.elitec.alejotaller.infraestructure.presentation.screens.HomeScreen
import com.elitec.alejotaller.infraestructure.presentation.screens.LoginScreen
import com.elitec.alejotaller.infraestructure.presentation.screens.SplashScreen

@Composable
fun MainNavigationWrapper(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(MainRoutesKey.Splash)
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(250)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250)
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250)
            )
        },
        entryProvider = entryProvider {
            entry<MainRoutesKey.Splash> {
                SplashScreen(
                    onInitChargeReady = { backStack.navigateTo(MainRoutesKey.Login) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Home> { key ->
                HomeScreen(
                    onNavigateBack = { backStack.navigateBack() },
                    userId = key.userId,
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Landing> {
                Text("Error")
            }
            entry<MainRoutesKey.Login> {
                LoginScreen(
                    onNavigateTo = { route -> backStack.navigateTo(route) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Details> { key ->
                DetailScreen(
                    onNavigateBack = { backStack.navigateBack() },
                    id = key.id,
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Error> {
                Text(text = "Error")
            }
        },
    )
}
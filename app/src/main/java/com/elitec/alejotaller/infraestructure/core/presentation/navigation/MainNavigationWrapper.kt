package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.elitec.alejotaller.infraestructure.core.presentation.screens.DetailScreen
import com.elitec.alejotaller.infraestructure.core.presentation.screens.LoginScreen
import com.elitec.alejotaller.infraestructure.core.presentation.screens.RegisterScreen
import com.elitec.alejotaller.infraestructure.core.presentation.screens.SplashScreen
import com.elitec.alejotaller.infraestructure.extents.presentation.navigateBack
import com.elitec.alejotaller.infraestructure.extents.presentation.navigateTo

@Composable
fun MainNavigationWrapper(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(MainRoutesKey.Splash)
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.navigateBack() },
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
            entry<MainRoutesKey.MainHome> { key ->
                InternalNavigationWrapper(
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
            entry<MainRoutesKey.Register> {
                RegisterScreen(
                    onNavigateBack = { backStack.navigateBack() },
                    onRegisterReady = { userId -> backStack.navigateTo(MainRoutesKey.MainHome(userId)) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Error> {
                Text(text = "Error")
            }
        },
    )
}
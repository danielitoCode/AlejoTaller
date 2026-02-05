package com.elitec.alejotaller.infraestructure.presentation.navigation

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
        entryProvider = entryProvider {
            entry<MainRoutesKey.Splash> {
                SplashScreen(
                    onInitChargeReady = { backStack.add(MainRoutesKey.Login) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Home> { key ->
                HomeScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    userId = key.userId,
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Landing> {
                Text("Error")
            }
            entry<MainRoutesKey.Login> {
                LoginScreen(
                    onNavigateTo = { route -> backStack.add(route) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Details> { key ->
                DetailScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    id = key.id,
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<MainRoutesKey.Error> {
                Text(text = "Error")
            }
        }
    )
}
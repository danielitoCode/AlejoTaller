package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.elitec.alejotaller.infraestructure.core.presentation.screens.DetailScreen
import com.elitec.alejotaller.feature.auth.presentation.screen.LoginScreen
import com.elitec.alejotaller.feature.auth.presentation.screen.RegisterScreen
import com.elitec.alejotaller.infraestructure.core.presentation.screens.LandScreen
import com.elitec.alejotaller.feature.auth.presentation.screen.SplashScreen
import com.elitec.alejotaller.infraestructure.core.presentation.extents.navigateBack
import com.elitec.alejotaller.infraestructure.core.presentation.extents.navigateTo
import dev.tmapps.konnection.Konnection

@Composable
fun MainNavigationWrapper(
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(MainRoutesKey.Splash)
    val connectionStatus by Konnection.instance.observeHasConnection().collectAsStateWithLifecycle(false)
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = modifier.fillMaxSize()
    ) {
        NavDisplay(
            modifier = Modifier.fillMaxSize(),
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
                        onUserAuth = { userId ->
                            backStack.navigateTo(MainRoutesKey.MainHome(userId))
                        },
                        onUserNotAuth = {
                            backStack.navigateTo(MainRoutesKey.Landing)
                        },
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
                    LandScreen(
                        modifier = Modifier.fillMaxSize(),
                        onSignInClick = { backStack.navigateTo(MainRoutesKey.Login) },
                        onSignUpClick = { backStack.navigateTo(MainRoutesKey.Register) }
                    )
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
        AnimatedVisibility(
            visible = connectionStatus,
            modifier = Modifier.padding(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                tonalElevation = 3.dp,
                shadowElevation = 5.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalWifiConnectedNoInternet4,
                        contentDescription = "Offline",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        text = "Desconectado"
                    )
                }
            }
        }
    }
}
package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.elitec.alejotaller.feature.product.presentation.screen.ProductScreen
import com.elitec.alejotaller.infraestructure.core.presentation.screens.nested.HomeScreen

@Composable
fun InternalNavigationWrapper(
    onNavigateBack: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(InternalRoutesKey.Home)

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        popTransitionSpec = { fadeIn() togetherWith fadeOut() },
        predictivePopTransitionSpec = { fadeIn() togetherWith fadeOut() },
        entryProvider = entryProvider {
            entry<InternalRoutesKey.Home> {
                ProductScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<InternalRoutesKey.ProductDetail> { key ->

            }
            entry<InternalRoutesKey.Profile> {

            }
            entry<InternalRoutesKey.Buy> {

            }
            entry<InternalRoutesKey.BuyConfirm> {

            }
        }
    )
}
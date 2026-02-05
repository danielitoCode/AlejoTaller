package com.elitec.alejotaller.infraestructure.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay

@Composable
fun InternalNavigationWrapper(
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
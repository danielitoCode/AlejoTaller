package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.elitec.alejotaller.feature.product.data.test.productTestList
import com.elitec.alejotaller.feature.product.presentation.screen.ProductDetailScreen
import com.elitec.alejotaller.feature.product.presentation.screen.ProductDetailsPlaceholder
import com.elitec.alejotaller.feature.product.presentation.screen.ProductScreen
import com.elitec.alejotaller.infraestructure.core.presentation.components.BottomNavigationBar
import com.elitec.alejotaller.infraestructure.core.presentation.screens.nested.HomeScreen
import com.elitec.alejotaller.infraestructure.extents.presentation.navigateBack
import com.elitec.alejotaller.infraestructure.extents.presentation.navigateTo

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun InternalNavigationWrapper(
    onNavigateBack: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(InternalRoutesKey.Home)

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailSceneStrategy = rememberListDetailSceneStrategy<Any>(directive = directive)

    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = backStack,
        sceneStrategy = listDetailSceneStrategy,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        popTransitionSpec = { fadeIn() togetherWith fadeOut() },
        predictivePopTransitionSpec = { fadeIn() togetherWith fadeOut() },
        entryProvider = entryProvider {
            entry<InternalRoutesKey.Home>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        ProductDetailsPlaceholder(modifier = Modifier.fillMaxSize())
                    }
                )
            ) {
                ProductScreen(
                    navigateToDetails = { productId ->
                        backStack.navigateTo(InternalRoutesKey.ProductDetail(productId))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<InternalRoutesKey.ProductDetail>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                ProductDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    product = productTestList.first { it.id == key.productId},
                    onBackClick = { backStack.navigateBack() }
                )
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
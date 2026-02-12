package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.GifBox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dokar.sonner.ToastType
import com.elitec.alejotaller.feature.auth.presentation.screen.ProfileScreen
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.ProfileViewModel
import com.elitec.alejotaller.feature.product.data.test.productTestList
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import com.elitec.alejotaller.feature.product.presentation.screen.ProductDetailScreen
import com.elitec.alejotaller.feature.product.presentation.screen.ProductDetailsPlaceholder
import com.elitec.alejotaller.feature.product.presentation.screen.ProductScreen
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ProductViewModel
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ShopCartViewModel
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import com.elitec.alejotaller.feature.sale.presentation.screen.BuyScreen
import com.elitec.alejotaller.infraestructure.core.presentation.components.FloatingActionButtonMenu
import com.elitec.alejotaller.infraestructure.core.presentation.uiModels.FabMenuItem
import com.elitec.alejotaller.infraestructure.core.presentation.extents.navigateBack
import com.elitec.alejotaller.infraestructure.core.presentation.extents.navigateTo
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.util.UUID
import kotlin.time.Clock

@Suppress("LambdaParameterInEffect")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun InternalNavigationWrapper(
    onNavigateBack: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier,
    shopCartViewModel: ShopCartViewModel = koinViewModel(),
    productViewModel: ProductViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    toasterViewModel: ToasterViewModel = koinViewModel()
) {
    val backStack = rememberNavBackStack(InternalRoutesKey.Home)

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailSceneStrategy = rememberListDetailSceneStrategy<Any>(directive = directive)

    val profileInfo by profileViewModel.userProfile.collectAsStateWithLifecycle()
    val products by productViewModel.productFlow.collectAsStateWithLifecycle()
    val cartItems by shopCartViewModel.shopCartFlow.collectAsStateWithLifecycle()

    LaunchedEffect(null) {
        profileViewModel.getAccountInfo(
            onGetInfo = {
                toasterViewModel.showMessage("Informacion de usuario cargada", ToastType.Success)
            },
            onFail = {
                toasterViewModel.showMessage("No se pudo cargar informacion de usuario", ToastType.Error)
                onNavigateBack()
            }
        )
    }

    val fabItems = listOf(
        FabMenuItem("Productos", Icons.Default.GifBox, InternalRoutesKey.Home),
        FabMenuItem("Su Compra", Icons.Default.ShoppingCart, InternalRoutesKey.Buy),
        FabMenuItem("Perfil", Icons.Default.AccountCircle, InternalRoutesKey.Profile),
        FabMenuItem("Ajustes", Icons.Default.Settings, InternalRoutesKey.Settings)
    )

    profileInfo?.let { info ->
        NavDisplay(
            modifier = modifier.fillMaxSize(),
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
                    ProfileScreen(
                        profileName = "Usuario de pruebas",
                        profileEmail = "email@test.com",
                        navigateBack = {},
                        onEditProfile = {},
                        isGoogleUser = info.userProfile.sub != "",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<InternalRoutesKey.Buy> {
                    BuyScreen(
                        items = cartItems,
                        totalAmount = shopCartViewModel.getTotalAmount(),
                        onIncreaseQuantity = { productId ->
                            cartItems.firstOrNull { it.product.id == productId }?.let { item ->
                                shopCartViewModel.updateProductQuantity(productId, item.quantity + 1)
                            }
                        },
                        onDecreaseQuantity = { productId ->
                            cartItems.firstOrNull { it.product.id == productId }?.let { item ->
                                shopCartViewModel.updateProductQuantity(productId, item.quantity - 1)
                            }
                        },
                        onRemoveItem = { productId ->
                            cartItems.firstOrNull { it.product.id == productId }?.let { item ->
                                shopCartViewModel.removeProductFromShopCart(item.product)
                            }
                        },
                        onConfirmClick = {
                            if (cartItems.isNotEmpty()) {
                                backStack.navigateTo(InternalRoutesKey.BuyConfirm)
                            } else {
                                toasterViewModel.showMessage("El carrito está vacío", ToastType.Error)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<InternalRoutesKey.BuyConfirm> {

                }
                entry<InternalRoutesKey.Settings> {
                    Text(text = "AJUSTES")
                }
            }
        )
    }

    FloatingActionButtonMenu(
        items = fabItems,
        onNavigate = { route -> backStack.navigateTo(route) }
    )
}

private fun List<UiSaleItem>.toSale(userId: String): Sale {
    val products = map { item ->
        SaleItem(productId = item.product.id, quantity = item.quantity)
    }

    return Sale(
        id = UUID.randomUUID().toString(),
        date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        amount = sumOf { item -> item.product.price * item.quantity },
        products = products,
        userId = userId
    )
}
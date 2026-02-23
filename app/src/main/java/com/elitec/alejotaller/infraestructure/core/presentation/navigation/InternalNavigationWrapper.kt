package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.GifBox
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dokar.sonner.ToastType
import com.elitec.alejotaller.feature.auth.presentation.screen.ProfileScreen
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.AuthViewModel
import com.elitec.alejotaller.feature.auth.presentation.viewmodel.ProfileViewModel
import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.notifications.presentation.PromotionViewModel
import com.elitec.alejotaller.feature.notifications.presentation.screen.PromotionDetailScreen
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import com.elitec.alejotaller.feature.product.presentation.screen.ProductDetailScreen
import com.elitec.alejotaller.feature.product.presentation.screen.ProductDetailsPlaceholder
import com.elitec.alejotaller.feature.product.presentation.screen.ProductScreen
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ProductViewModel
import com.elitec.alejotaller.feature.product.presentation.viewmodel.ShopCartViewModel
import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import com.elitec.alejotaller.feature.sale.presentation.screen.BuyConfirmScreen
import com.elitec.alejotaller.feature.sale.presentation.screen.BuyReservationScreen
import com.elitec.alejotaller.feature.sale.presentation.screen.BuyScreen
import com.elitec.alejotaller.feature.sale.presentation.viewmodel.SaleViewModel
import com.elitec.alejotaller.feature.settigns.presentation.screen.SettingsScreen
import com.elitec.alejotaller.infraestructure.core.presentation.components.FloatingActionButtonMenu
import com.elitec.alejotaller.infraestructure.core.presentation.extents.navigateBack
import com.elitec.alejotaller.infraestructure.core.presentation.extents.navigateTo
import com.elitec.alejotaller.infraestructure.core.presentation.uiModels.FabMenuItem
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.RealtimeSyncViewModel
import com.elitec.alejotaller.infraestructure.core.presentation.viewmodel.ToasterViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.util.UUID
import kotlin.time.Clock
import androidx.core.net.toUri
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.infraestructure.core.presentation.util.rememberAdaptiveLayoutSpec

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
    toasterViewModel: ToasterViewModel = koinViewModel(),
    promotionViewModel: PromotionViewModel = koinViewModel(),
    realtimeSyncViewModel: RealtimeSyncViewModel = koinViewModel(),
    saleViewModel: SaleViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
) {
    val backStack = rememberNavBackStack(InternalRoutesKey.Home)

    var isSubmittingPurchase by remember { mutableStateOf(false) }
    var isUpdatingDeliveryType by remember { mutableStateOf(false) }

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val layoutSpec = rememberAdaptiveLayoutSpec()
    val listDetailSceneStrategy = rememberListDetailSceneStrategy<Any>(directive = directive)

    val searchQuery by productViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by productViewModel.selectedCategoryId.collectAsStateWithLifecycle()

    val profileInfo by profileViewModel.userProfile.collectAsStateWithLifecycle()
    val products by productViewModel.productFlow.collectAsStateWithLifecycle()
    val cartItems by shopCartViewModel.shopCartFlow.collectAsStateWithLifecycle()
    val sales by saleViewModel.salesFlow.collectAsStateWithLifecycle()
    val promotions by promotionViewModel.promotionsFlow.collectAsStateWithLifecycle()
    val pendingSaleIds = remember(userId) {
        sales
            .asSequence()
            .filter { sale -> sale.userId == userId && sale.verified == BuyState.UNVERIFIED }
            .map { sale -> sale.id }
            .toSet()
    }
    val hasPendingSales = pendingSaleIds.isNotEmpty()

    LaunchedEffect(null) {
        toasterViewModel.showMessage("Cargando productos", ToastType.Normal, id = "product charge", isInfinite = true)
        productViewModel.syncProducts(
            onProductCharge = {
                toasterViewModel.dismissMessage("product charge")
                toasterViewModel.showMessage("Productos cargados", ToastType.Success)
            },
            onFail = {
                toasterViewModel.dismissMessage("product charge")
                toasterViewModel.showMessage("Error al cargar los productos", ToastType.Error)
            }
        )
    }

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

    LaunchedEffect(userId) {
        saleViewModel.sync(userId)
    }

    LaunchedEffect(userId, pendingSaleIds) {
        realtimeSyncViewModel.updateSubscriptionScope(userId = userId, pendingSaleIds = pendingSaleIds)
    }

    LaunchedEffect(hasPendingSales) {
        if (hasPendingSales) {
            realtimeSyncViewModel.startRealtimeSync()
        } else {
            realtimeSyncViewModel.stopRealtimeSync()
        }
    }

    LaunchedEffect(null) {
        realtimeSyncViewModel.uiMessages.collect { message ->
            toasterViewModel.showMessage(
                message = message.message,
                type = if (message.isError) ToastType.Error else ToastType.Success
            )
        }
    }

    val fabItems = listOf(
        FabMenuItem("Productos", Icons.Default.GifBox, InternalRoutesKey.Home),
        FabMenuItem("Su Compra", Icons.Default.ShoppingCart, InternalRoutesKey.Buy),
        FabMenuItem("Reservas", Icons.Default.QrCode, InternalRoutesKey.BuyReservation),
        FabMenuItem("Perfil", Icons.Default.AccountCircle, InternalRoutesKey.Profile),
        FabMenuItem("Ajustes", Icons.Default.Settings, InternalRoutesKey.Settings),
        FabMenuItem("Cerrar Sesión", Icons.Default.Logout, InternalRoutesKey.Logout)
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
                        products = products,
                        promotions = promotions,
                        searchQuery = searchQuery,
                        selectedCategoryId = selectedCategoryId,
                        onSearchQueryChanged = { query ->
                            productViewModel.onSearchQueryChanged(query)
                        },
                        onCategorySelected = { categoryId ->
                            productViewModel.onCategorySelected(categoryId)
                        },
                        navigateToDetails = { productId ->
                            backStack.navigateTo(InternalRoutesKey.ProductDetail(productId))
                        },
                        onPromotionClick = { promotionId ->
                            backStack.navigateTo(InternalRoutesKey.PromotionDetail(promotionId))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<InternalRoutesKey.PromotionDetail>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { key ->
                    PromotionDetailScreen(
                        modifier = Modifier.fillMaxSize(),
                        promotion = promotions.firstOrNull { it.id == key.promotionId }
                            ?: fallbackPromotion(key.promotionId),
                        onBackClick = { backStack.navigateBack() }
                    )
                }
                entry<InternalRoutesKey.ProductDetail>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { key ->
                    ProductDetailScreen(
                        modifier = Modifier.fillMaxSize(),
                        product = products.first { it.id == key.productId },
                        showTopBar = layoutSpec.showTopBarInDetail,
                        onBackClick = { backStack.navigateBack() },
                        onAddToCartClick = {
                            val productSelected = products.first { it.id == key.productId }
                            shopCartViewModel.addProductToACart(productSelected, 1)
                        }
                    )
                }
                entry<InternalRoutesKey.Profile> {
                    ProfileScreen(
                        profileName = profileInfo?.name ?: "Usuario",
                        profileEmail = profileInfo?.email ?: "Sin correo",
                        userId = userId,
                        profilePhone = profileInfo?.userProfile?.phone,
                        profilePhotoUrl = profileInfo?.userProfile?.photoUrl,
                        navigateBack = { backStack.navigateBack() },
                        onEditProfile = {
                            profileViewModel.getAccountInfo(onGetInfo = {}, onFail = {})
                        },
                        isGoogleUser = profileInfo?.userProfile?.sub?.isNotBlank() == true,
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
                entry<InternalRoutesKey.BuyReservation> {
                    BuyReservationScreen(
                        sales = sales.filter { it.userId == userId },
                        productNamesById = products.associate { it.id to it.name },
                        // ← Nuevo: mostrar home cuando está vacío
                        onGoToShop = {
                            backStack.navigateTo(InternalRoutesKey.Home)
                        },
                        // ← Nuevo: guardar preferencia de entrega
                        onDeliveryTypeSelected = { saleId, deliveryType ->
                            if (isUpdatingDeliveryType) {
                                return@BuyReservationScreen
                            }
                            isUpdatingDeliveryType = true
                            saleViewModel.updateDeliveryType(
                                saleId = saleId,
                                deliveryType = deliveryType,
                                onSuccess = {
                                    isUpdatingDeliveryType = false
                                    val msg = when (deliveryType) {
                                        DeliveryType.PICKUP -> "¡Perfecto! Te esperamos en el taller 🏪"
                                        DeliveryType.DELIVERY -> "¡Listo! Coordinaremos la entrega contigo 🛵"
                                    }
                                    toasterViewModel.showMessage(msg, ToastType.Success)
                                },
                                onFail = { error ->
                                    isUpdatingDeliveryType = false
                                    toasterViewModel.showMessage(
                                        "No se pudo guardar la preferencia de entrega. Reintenta. Detalle: $error",
                                        ToastType.Error
                                    )
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<InternalRoutesKey.BuyConfirm> {
                    val context = LocalContext.current

                    BuyConfirmScreen(
                        items = cartItems,
                        totalAmount = shopCartViewModel.getTotalAmount(),
                        onBackClick = { backStack.navigateBack() },
                        onSubmitPurchase = {
                            if (isSubmittingPurchase) return@BuyConfirmScreen
                            if (cartItems.isEmpty()) {
                                toasterViewModel.showMessage("El carrito está vacío", ToastType.Error)
                                backStack.navigateBack()
                            } else {
                                isSubmittingPurchase = true
                                toasterViewModel.showMessage(
                                    "Procesando pedido…",
                                    ToastType.Normal,
                                    id = "sale_charge",
                                    isInfinite = true
                                )
                                saleViewModel.initiatePayment(
                                    sale = cartItems.toSale(userId),
                                    onReadyToPay = { saleId, checkoutUrl ->
                                        isSubmittingPurchase = false
                                        toasterViewModel.dismissMessage("sale_charge")
                                        shopCartViewModel.clearCart()
                                        if (checkoutUrl != null) {
                                            // ✅ Abre la URL de pago en Chrome Custom Tabs
                                            val intent = androidx.browser.customtabs.CustomTabsIntent.Builder()
                                                .setShowTitle(true)
                                                .build()
                                            intent.launchUrl(context, checkoutUrl.toUri())
                                            // Navegar a reservas para que el usuario vea su pedido pendiente
                                            backStack.navigateTo(InternalRoutesKey.BuyReservation)
                                        } else {
                                            // Pago online no disponible, pero la venta fue registrada
                                            toasterViewModel.showMessage(
                                                "Pedido registrado (#${saleId.take(8)}). " +
                                                        "El pago online no está disponible. " +
                                                        "Contacta al taller para coordinar el pago.",
                                                ToastType.Warning
                                            )
                                            backStack.navigateTo(InternalRoutesKey.BuyReservation)
                                        }
                                    },
                                    onFail = { error ->
                                        isSubmittingPurchase = false
                                        toasterViewModel.dismissMessage("sale_charge")
                                        toasterViewModel.showMessage(
                                            "No se pudo procesar el pedido: $error",
                                            ToastType.Error
                                        )
                                    }
                                )
                            }
                        },
                        onRegisterInUltrapay = {},
                        isSubmitting = isSubmittingPurchase,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<InternalRoutesKey.Settings> {
                    SettingsScreen(
                        asDetailPane = layoutSpec.showListAndDetail,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        )
    }

    FloatingActionButtonMenu(
        items = fabItems,
        shopCartItemsCount = cartItems.size,
        onNavigate = { route -> backStack.navigateTo(route) },
        onLogout = {
            authViewModel.logoutUser(
                onLogout = {
                    onNavigateBack()
                },
                onFail = {
                    onNavigateBack()
                }
            )

        }
    )
}

private fun List<UiSaleItem>.toSale(userId: String): Sale {
    val products = map { item ->
        SaleItem(productId = item.product.id, quantity = item.quantity, productName = item.product.name)
    }

    return Sale(
        id = UUID.randomUUID().toString(),
        date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
        amount = sumOf { item -> item.product.price * item.quantity },
        products = products,
        verified = BuyState.UNVERIFIED,
        userId = userId
    )
}

private fun fallbackPromotion(id: String): Promotion {
    val now = System.currentTimeMillis()
    return Promotion(
        id = id,
        title = "Oferta",
        message = "Promoción no disponible",
        imageUrl = null,
        validFromEpochMillis = now,
        validUntilEpochMillis = now
    )
}
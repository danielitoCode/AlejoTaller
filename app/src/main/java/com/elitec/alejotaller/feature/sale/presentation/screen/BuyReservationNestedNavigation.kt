package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.infraestructure.core.presentation.navigation.InternalRoutesKey

@Composable
fun BuyReservationNestedNavigation(
    sales: List<Sale>,
    findProductPrice:(String) -> Double,
    productNamesById: Map<String, String>,
    modifier: Modifier = Modifier,
    // ── Nuevo callback para cuando el usuario confirma tipo de entrega ──
    onDeliveryTypeSelected: (saleId: String, type: DeliveryType) -> Unit = { _, _ -> },
    // ── Nuevo callback para navegar al carrito (desde el EmptyState) ──
    onGoToShop: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(ReservationNestedRoutes.Reservations)

    when (sales.isNotEmpty()) {
        true -> {
            NavDisplay(
                backStack = backStack,
                modifier = modifier,
                predictivePopTransitionSpec = { fadeIn() togetherWith fadeOut() },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<ReservationNestedRoutes.Reservations> {
                        BuyReservationScreen(
                            sales = sales,
                            onGoToShop = onGoToShop,
                            onSaleSelected = { saleSelectedId ->
                                backStack.add(
                                    ReservationNestedRoutes.ReservationDetails(saleSelectedId)
                                )
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    entry<ReservationNestedRoutes.ReservationDetails> { key ->
                        BuyReservationDetailsScreen(
                            sale = sales.first { it.id == key.reservationId },
                            findProductPrice = findProductPrice,
                            productNamesById = productNamesById,
                            onDeliveryTypeSelected = onDeliveryTypeSelected,
                            modifier = Modifier.fillMaxSize().padding(
                                vertical = 10.dp,
                                horizontal = 15.dp
                            )
                        )
                    }
                }
            )
        }
        else -> {}
    }
}
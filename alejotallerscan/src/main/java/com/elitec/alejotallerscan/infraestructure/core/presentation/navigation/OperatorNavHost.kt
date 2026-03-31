package com.elitec.alejotallerscan.infraestructure.core.presentation.navigation

import androidx.compose.runtime.Composable
import com.elitec.alejotallerscan.feature.auth.presentation.screen.OperatorLoginScreen
import com.elitec.alejotallerscan.feature.confirmation.presentation.screen.OperatorConfirmPaymentScreen
import com.elitec.alejotallerscan.feature.home.presentation.screen.OperatorHomeScreen
import com.elitec.alejotallerscan.feature.reservation.presentation.screen.OperatorReservationsScreen
import com.elitec.alejotallerscan.feature.scan.presentation.screen.OperatorScanScreen

@Composable
fun OperatorNavHost(
    currentDestination: OperatorDestination,
    onNavigate: (OperatorDestination) -> Unit
) {
    when (currentDestination) {
        OperatorDestination.Login -> OperatorLoginScreen(
            onLoginSuccess = { onNavigate(OperatorDestination.Home) }
        )

        OperatorDestination.Home -> OperatorHomeScreen(
            onOpenScan = { onNavigate(OperatorDestination.Scan) },
            onOpenReservations = { onNavigate(OperatorDestination.Reservations) },
            onOpenConfirmation = { onNavigate(OperatorDestination.ConfirmPayment) },
            onLogout = { onNavigate(OperatorDestination.Login) }
        )

        OperatorDestination.Scan -> OperatorScanScreen(
            onBack = { onNavigate(OperatorDestination.Home) },
            onProceedToConfirm = { onNavigate(OperatorDestination.ConfirmPayment) }
        )

        OperatorDestination.ConfirmPayment -> OperatorConfirmPaymentScreen(
            onBack = { onNavigate(OperatorDestination.Home) },
            onOpenScan = { onNavigate(OperatorDestination.Scan) }
        )

        OperatorDestination.Reservations -> OperatorReservationsScreen(
            onBack = { onNavigate(OperatorDestination.Home) },
            onOpenSale = { onNavigate(OperatorDestination.ConfirmPayment) }
        )
    }
}

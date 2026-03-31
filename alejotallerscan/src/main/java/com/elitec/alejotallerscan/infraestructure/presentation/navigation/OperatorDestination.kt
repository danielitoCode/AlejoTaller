package com.elitec.alejotallerscan.infraestructure.presentation.navigation

sealed interface OperatorDestination {
    data object Login : OperatorDestination
    data object Home : OperatorDestination
    data object Scan : OperatorDestination
    data object ConfirmPayment : OperatorDestination
    data object Reservations : OperatorDestination
}

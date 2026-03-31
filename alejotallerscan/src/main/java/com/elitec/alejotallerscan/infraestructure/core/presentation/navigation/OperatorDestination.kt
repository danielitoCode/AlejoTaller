package com.elitec.alejotallerscan.infraestructure.core.presentation.navigation

sealed interface OperatorDestination {
    val route: String

    data object Login : OperatorDestination {
        override val route: String = "login"
    }

    data object Home : OperatorDestination {
        override val route: String = "home"
    }

    data object Scan : OperatorDestination {
        override val route: String = "scan"
    }

    data object ConfirmPayment : OperatorDestination {
        override val route: String = "confirm-payment"
    }

    data object Reservations : OperatorDestination {
        override val route: String = "reservations"
    }

    companion object {
        fun fromRoute(route: String): OperatorDestination =
            when (route) {
                Login.route -> Login
                Home.route -> Home
                Scan.route -> Scan
                ConfirmPayment.route -> ConfirmPayment
                Reservations.route -> Reservations
                else -> Login
            }
    }
}

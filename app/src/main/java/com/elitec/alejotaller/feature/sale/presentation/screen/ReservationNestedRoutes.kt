package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class ReservationNestedRoutes : NavKey {
    @Serializable
    object Reservations: ReservationNestedRoutes()
    @Serializable
    data class ReservationDetails(val reservationId: String): ReservationNestedRoutes()
}
package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class MainRoutesKey: NavKey {
    @Serializable
    object Splash: MainRoutesKey()

    @Serializable
    object Landing: MainRoutesKey()

    @Serializable
    object Login: MainRoutesKey()

    @Serializable
    data class MainHome(
        val userId: String,
        val pendingReservationId: String? = null,
        val pendingProductId: String? = null,
        /** AUTH_POLICY: true when session is visitor / anonymous */
        val isGuest: Boolean = false
    ): MainRoutesKey()

    @Serializable
    object Register: MainRoutesKey()

    @Serializable
    object Error: MainRoutesKey()

    // From Test only
    @Serializable
    data class Details(val id: String): MainRoutesKey()
}

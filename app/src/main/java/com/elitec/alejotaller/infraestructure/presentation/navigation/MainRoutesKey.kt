package com.elitec.alejotaller.infraestructure.presentation.navigation

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
    data class Home(val userId: String): MainRoutesKey()

    @Serializable
    object Register: MainRoutesKey()

    @Serializable
    object Error: MainRoutesKey()

    // From Test only
    @Serializable
    data class Details(val id: String): MainRoutesKey()
}
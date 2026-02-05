package com.elitec.alejotaller.infraestructure.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class MainRoutesKey {
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
}
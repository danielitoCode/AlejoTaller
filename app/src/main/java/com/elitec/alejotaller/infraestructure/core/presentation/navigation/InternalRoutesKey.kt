package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class InternalRoutesKey: NavKey {
    @Serializable
    object Home: InternalRoutesKey()

    @Serializable
    data class ProductDetail(val productId: String): InternalRoutesKey()

    @Serializable
    object Profile: InternalRoutesKey()

    @Serializable
    data class PromotionDetail(val promotionId: String): InternalRoutesKey()

    @Serializable
    object Buy: InternalRoutesKey()

    @Serializable
    object BuyConfirm: InternalRoutesKey()

    @Serializable
    object BuyReservation: InternalRoutesKey()

    @Serializable
    object Settings: InternalRoutesKey()

    @Serializable object Logout: InternalRoutesKey()
}
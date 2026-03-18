package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class BuyReservationDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeSale = Sale(
        id = "SALE-12345678",
        date = LocalDate(2023, 10, 20),
        amount = 5000.0,
        verified = BuyState.VERIFIED,
        products = listOf(
            SaleItem(productId = "p1", quantity = 2, productName = "Aceite Motor")
        ),
        userId = "user1"
    )

    @Test
    fun when_sale_is_verified_delivery_selection_is_shown() {
        composeTestRule.setContent {
            AlejoTallerTheme {
                BuyReservationDetailsScreen(
                    sale = fakeSale,
                    findProductPrice = { 2500.0 },
                    productNamesById = mapOf("p1" to "Aceite Motor"),
                    onDeliveryTypeSelected = { _, _ -> }
                )
            }
        }

        // Verificar que la sección de entrega es visible
        composeTestRule.onNodeWithTag(DELIVERY_SECTION_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("🎉 ¡Tu pedido está listo!").assertIsDisplayed()
    }

    @Test
    fun when_sale_is_unverified_delivery_selection_is_hidden() {
        composeTestRule.setContent {
            AlejoTallerTheme {
                BuyReservationDetailsScreen(
                    sale = fakeSale.copy(verified = BuyState.UNVERIFIED),
                    findProductPrice = { 2500.0 },
                    productNamesById = mapOf("p1" to "Aceite Motor"),
                    onDeliveryTypeSelected = { _, _ -> }
                )
            }
        }

        // La sección de entrega NO debería existir
        composeTestRule.onNodeWithTag(DELIVERY_SECTION_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithText("🎉 ¡Tu pedido está listo!").assertDoesNotExist()
    }

    @Test
    fun when_clicking_pickup_option_callback_is_invoked() {
        var selectedId = ""
        var selectedType: DeliveryType? = null

        composeTestRule.setContent {
            AlejoTallerTheme {
                BuyReservationDetailsScreen(
                    sale = fakeSale.copy(deliveryType = null),
                    findProductPrice = { 2500.0 },
                    productNamesById = mapOf("p1" to "Aceite Motor"),
                    onDeliveryTypeSelected = { id, type ->
                        selectedId = id
                        selectedType = type
                    }
                )
            }
        }

        // Simular clic en Recoger
        composeTestRule.onNodeWithTag(OPTION_PICKUP_TAG).performClick()

        // Verificar resultados
        assertEquals(fakeSale.id, selectedId)
        assertEquals(DeliveryType.PICKUP, selectedType)
    }

    @Test
    fun when_delivery_type_is_selected_confirmation_message_appears() {
        composeTestRule.setContent {
            AlejoTallerTheme {
                BuyReservationDetailsScreen(
                    sale = fakeSale.copy(deliveryType = DeliveryType.DELIVERY),
                    findProductPrice = { 2500.0 },
                    productNamesById = mapOf("p1" to "Aceite Motor"),
                    onDeliveryTypeSelected = { _, _ -> }
                )
            }
        }

        // Verificar mensaje de domicilio
        composeTestRule.onNodeWithText("✅ ¡Entendido! El taller te contactará pronto para coordinar la entrega.")
            .assertIsDisplayed()
    }
}

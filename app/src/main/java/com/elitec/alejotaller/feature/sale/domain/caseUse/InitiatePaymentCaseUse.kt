package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.PaymentGateway
import com.elitec.alejotaller.feature.sale.domain.repository.SaleNotificationUserProvider
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator
import io.appwrite.ID

/**
 * Orquesta el flujo completo de registro + inicio de pago:
 *
 * 1. Asigna ID único a la venta
 * 2. Notifica por Telegram al taller
 * 3. Guarda la venta localmente (estado: UNVERIFIED)
 * 4. Solicita a la pasarela la URL de checkout
 *
 * Retorna la URL de checkout para que la capa de presentación
 * la abra en Chrome Custom Tabs.
 *
 * Si la pasarela falla, la venta ya está guardada localmente y
 * notificada por Telegram — el taller puede gestionarla manualmente.
 */
class InitiatePaymentCaseUse(
    private val repository: SaleRepository,
    private val notificationUserProvider: SaleNotificationUserProvider,
    private val telegramNotificator: TelegramNotificator,
    private val paymentGateway: PaymentGateway
) {
    suspend operator fun invoke(sale: Sale): Result<PaymentInitResult> = runCatching {
        // Paso 1: Asignar ID definitivo a la venta
        val saleConfirmed = sale.copy(id = ID.unique())
        // Paso 2: Obtener datos del usuario para la notificación
        val user = notificationUserProvider
            .getCurrentUser()
            .getOrElse { throw Exception("No se pudo obtener el usuario para la notificación") }
        // Paso 3: Notificar al taller por Telegram
        telegramNotificator.notify(saleConfirmed, user)
        // Paso 4: Guardar la venta localmente (offline-first)
        repository.save(saleConfirmed)
        // Paso 5: Solicitar URL de pago a la pasarela
        val description = buildDescription(saleConfirmed)
        val checkoutUrlResult = paymentGateway.createCheckoutUrl(
            saleId = saleConfirmed.id,
            amount = saleConfirmed.amount,
            description = description
        )
        PaymentInitResult(
            saleId = saleConfirmed.id,
            checkoutUrl = checkoutUrlResult.getOrNull()  // null = Telegram OK pero pago fallido
        )
    }
    private fun buildDescription(sale: Sale): String {
        val itemCount = sale.products.sumOf { it.quantity }
        return "Pedido #${sale.id.take(8)} — $itemCount artículo(s) — ${"%.2f".format(sale.amount)} CUP"
    }
}
/**
 * Resultado de iniciar el pago:
 * - [saleId]: ID de la venta creada
 * - [checkoutUrl]: URL de la pasarela (null si la pasarela falló pero Telegram/DB OK)
 */
data class PaymentInitResult(
    val saleId: String,
    val checkoutUrl: String?
)
package com.elitec.shared.sale.feature.sale.domain.caseUse

import android.util.Log
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.repository.SaleIdProvider
import com.elitec.shared.sale.feature.sale.domain.repository.SaleNotificationUserProvider
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository
import com.elitec.shared.sale.feature.sale.domain.repository.TelegramNotificator

/**
 * Registra una nueva venta.
 *
 * El guardado en repositorio es el camino crítico.
 * La notificación a Telegram es best-effort: un fallo de red/config
 * no debe impedir que el pedido quede persistido (local + remoto).
 */
class RegisterNewSaleCauseUse(
    private val repository: SaleRepository,
    private val saleIdProvider: SaleIdProvider,
    private val notificationUserProvider: SaleNotificationUserProvider,
    private val telegramNotificator: TelegramNotificator
) {
    suspend operator fun invoke(sale: Sale): Result<String> = runCatching {
        val saleConfirmed = sale.copy(id = saleIdProvider.nextId())

        // Camino crítico: persistir la venta
        repository.save(saleConfirmed)

        // Notificación operativa: no debe tumbar el checkout
        runCatching {
            val user = notificationUserProvider
                .getCurrentUser()
                .getOrElse { error ->
                    throw error
                }
            telegramNotificator.notify(saleConfirmed, user)
        }.onFailure { error ->
            Log.w(
                TAG,
                "event=telegram_notify_best_effort_failure saleId=${saleConfirmed.id} " +
                    "cause=${error.message}"
            )
        }

        saleConfirmed.id
    }

    companion object {
        private const val TAG = "RegisterNewSale"
    }
}

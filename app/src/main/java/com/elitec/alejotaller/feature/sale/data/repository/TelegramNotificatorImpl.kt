package com.elitec.alejotaller.feature.sale.data.repository

import android.util.Log
import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleNotifierUser
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class TelegramNotificatorImpl(
    private val httpClient: HttpClient
) : TelegramNotificator {

    private companion object {
        const val DEFAULT_TELEGRAM_API_URL = "https://api.telegram.org"
        const val TAG = "TelegramNotificator"
    }

    override suspend fun notify(sale: Sale, user: SaleNotifierUser) {
        val chatId = BuildConfig.TELEGRAM_CHAT_ID.trim()
        require(chatId.isNotBlank()) { "TELEGRAM_CHAT_ID no está configurado" }

        val endpoint = resolveTelegramSendMessageEndpoint()
        Log.i(TAG, "event=telegram_notify_start saleId=${sale.id} endpoint=$endpoint")

        val response = httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            headers.append(HttpHeaders.CacheControl, "no-cache")
            setBody(
                TelegramMessageRequest(
                    chatId = chatId,
                    text = sale.toFormattedMessage(user)
                )
            )
        }.body<TelegramApiResponse>()

        Log.i(TAG, "event=telegram_notify_response saleId=${sale.id} ok=${response.ok} errorCode=${response.errorCode}")

        check(response.ok) {
            "Telegram API devolvió error${response.errorCode?.let { " (code=$it)" } ?: ""}: ${response.description ?: "sin descripción"}"
        }

        Log.i(TAG, "event=telegram_notify_success saleId=${sale.id}")
    }

    private fun resolveTelegramSendMessageEndpoint(): String {
        val configuredUrl = BuildConfig.TELEGRAM_API_URL.trim().removeSuffix("/")
        val botToken = BuildConfig.TELEGRAM_BOT_KEY.trim()
        val baseUrl = configuredUrl.ifBlank { DEFAULT_TELEGRAM_API_URL }

        return when {
            configuredUrl.isBlank() -> {
                require(botToken.isNotBlank()) { "TELEGRAM_BOT_KEY no está configurado" }
                "$baseUrl/bot$botToken/sendMessage"
            }

            configuredUrl.endsWith("/sendMessage") -> configuredUrl
            configuredUrl.contains("/bot") -> "$baseUrl/sendMessage"

            else -> {
                require(botToken.isNotBlank()) { "TELEGRAM_BOT_KEY no está configurado" }
                "$baseUrl/bot$botToken/sendMessage"
            }
        }
    }
}

@Serializable
private data class TelegramMessageRequest(
    @SerialName("chat_id") val chatId: String,
    val text: String,
    @SerialName("parse_mode") val parseMode: String? = null
)

@Serializable
private data class TelegramApiResponse(
    val ok: Boolean,
    val description: String? = null,
    @SerialName("error_code") val errorCode: Int? = null
)

fun Sale.toFormattedMessage(user: SaleNotifierUser): String {
    val header = """
        🛍️ LISTA DE DESEOS
        👤 Usuario: ${user.name}
        📧 Correo: ${user.email}
        📱 Teléfono: ${user.phone ?: "No registrado"}

        📝 DETALLES DEL PEDIDO
    """.trimIndent()

    val itemsDetails = products.joinToString(separator = "\n") { item ->
        val productDisplayName = item.productName ?: "ID: ${item.productId}"
        "🔹 Producto: $productDisplayName | Cantidad: ${item.quantity}"
    }

    return "$header\n$itemsDetails\n\n💵 Monto total: ${"%.2f".format(amount)}"
}
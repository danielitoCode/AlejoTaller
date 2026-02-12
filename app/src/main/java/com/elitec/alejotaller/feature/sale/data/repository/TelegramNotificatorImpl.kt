package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleNotifierUser
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator
import io.ktor.client.HttpClient
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
    override suspend fun notify(sale: Sale, user: SaleNotifierUser) {
        val response = httpClient.post(BuildConfig.TELEGRAM_API_URL) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            headers.append(HttpHeaders.CacheControl, "no-cache")
            setBody(
                TelegramMessageRequest(
                    chatId = BuildConfig.TELEGRAM_CHAT_ID,
                    text = sale.toFormattedMessage(user),
                    parseMode = "Markdown"
                )
            )
        }


    }
}

@Serializable
private data class TelegramMessageRequest(
    @SerialName("chat_id") val chatId: String,
    val text: String,
    @SerialName("parse_mode") val parseMode: String = "Markdown"
)

fun Sale.toFormattedMessage(user: SaleNotifierUser): String {
    val header = """
        🛍️ *LISTA DE DESEOS*
        👤 Usuario: ${user.name}
        📧 Correo: ${user.email}
        📱 Teléfono: ${user.phone ?: "No registrado"}

        📝 *DETALLES DEL PEDIDO*
    """.trimIndent()

    val itemsDetails = products.joinToString(separator = "\n") { item ->
        "🔹 Producto ID: `${item.productId}` | Cantidad: *${item.quantity}*"
    }

    return "$header\n$itemsDetails\n\n💵 *Monto total:* ${"%.2f".format(amount)}"
}
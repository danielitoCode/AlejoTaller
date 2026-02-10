package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator
import io.appwrite.extensions.toJson
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI

class TelegramNotificatorImpl(
    private val httpClient: HttpClient
): TelegramNotificator {
    override suspend fun notify(sale: Sale, user: User) {
        httpClient.post(BuildConfig.TELEGRAM_API_URL) {
            contentType(ContentType.Application.Json)
            setBody(sale)
        }
    }
}

fun Sale.toFormatedMessage(user: User): String {
    val header = """
        🛍️ *LISTA DE DESEOS*
        👤 Usuario: ${user.name}
        📧 Correo: ${user.email}
        📱 Teléfono: ${user.userProfile.phone}
        
        📝 *DETALLES DEL PEDIDO*
    """.trimIndent()

    var itemsDetails = ""

    /*products.forEachIndexed { index, item ->
        itemsDetails += """
            
            🔹 *JOYA #${index + 1}*
            ✨ Nombre: ${item.joya.name}
            💰 Precio unitario: ${"%.2f".format(item.joya.price)} $
            📦 Cantidad: ${item.cantidad}
            🖼️ Foto: ${item.joya.photoUrl}
            
        """.trimIndent()
        totalAmount += item.joya.price * item.cantidad
    }*/
    return ""
}
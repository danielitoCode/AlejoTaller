package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator

class RegisterNewSaleCauseUse(
    private val repository: SaleRepository,
    private val telegramNotificator: TelegramNotificator
) {
    suspend operator fun invoke(sale: Sale): Result<Unit> =  runCatching {
        repository.save(sale)
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

    products.forEachIndexed { index, item ->
        itemsDetails += """
            
            🔹 *JOYA #${index + 1}*
            ✨ Nombre: ${item.name}
            💰 Precio unitario: ${"%.2f".format(item.joya.price)} $
            📦 Cantidad: ${item.cantidad}
            🖼️ Foto: ${item.joya.photoUrl}
            
        """.trimIndent()
        totalAmount += item.joya.price * item.cantidad
    }
    return ""
}
package com.elitec.shared.data.feature.sale.data.mapper

import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import io.appwrite.models.Document
import kotlinx.datetime.LocalDate

fun Document<Map<String, Any>>.toSaleDto(): SaleDto =
    SaleDto(
        id = id,
        date = data["date"].toLocalDate(),
        amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
        products = data["products"].toSaleItems(),
        verified =  data["verified"] as? String ?: "UNVERIFIED",
        userId = data["user_id"] as? String ?: "",
        customerName = data["customer_name"] as? String,
        deliveryType = data["delivery_type"] as? String,
        deliveryAddress = data["delivery_address"] as? String
    )

private fun Any?.toLocalDate(): LocalDate = when (this) {
    is LocalDate -> this
    is String -> parseToLocalDate(this)
    else -> LocalDate(1970, 1, 1)
}

private fun parseToLocalDate(rawDate: String): LocalDate =
    runCatching { LocalDate.parse(rawDate) }
        .getOrElse {
            val normalized = rawDate.substringBefore("T")
            runCatching { LocalDate.parse(normalized) }.getOrDefault(LocalDate(1970, 1, 1))
        }

private fun Any?.toSaleItems(): List<SaleItem> = when (this) {
    is List<*> -> this.mapNotNull { item ->
        when (item) {
            is String -> item.trim().takeIf { it.isNotBlank() }?.let { productId ->
                SaleItem(productId = productId, quantity = 1)
            }
            is Map<*, *> -> {
                val productId = listOf("productId", "product_id", "id", "\$id")
                    .firstNotNullOfOrNull { key -> (item[key] as? String)?.trim()?.takeIf { it.isNotBlank() } }
                    ?: return@mapNotNull null
                val quantity = (item["quantity"] as? Number)?.toInt() ?: 1
                val productName = listOf("productName", "product_name", "name", "title")
                    .firstNotNullOfOrNull { key -> (item[key] as? String)?.trim()?.takeIf { it.isNotBlank() } }
                SaleItem(productId = productId, quantity = quantity, productName = productName)
            }
            else -> null
        }
    }

    else -> emptyList()
}

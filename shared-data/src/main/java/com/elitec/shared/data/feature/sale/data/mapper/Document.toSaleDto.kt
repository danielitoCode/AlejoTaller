package com.elitec.shared.data.feature.sale.data.mapper

import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import io.appwrite.models.Document
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun Document<Map<String, Any>>.toSaleDto(): SaleDto =
    SaleDto(
        id = id,
        date = data["date"].toLocalDate(),
        amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
        products = data["products"].toSaleItems(),
        verified = data["buy_state"] as? String
            ?: data["verified"] as? String
            ?: "UNVERIFIED",
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
    is String -> parseSaleItemsFromJsonString(this)
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

private fun parseSaleItemsFromJsonString(raw: String): List<SaleItem> {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return emptyList()

    return runCatching {
        Json.parseToJsonElement(trimmed)
    }.getOrNull()
        ?.toSaleItemsFromJsonElement()
        ?: emptyList()
}

private fun JsonElement.toSaleItemsFromJsonElement(): List<SaleItem> = when (this) {
    is JsonArray -> this.mapNotNull { it.toSaleItemOrNull() }
    else -> emptyList()
}

private fun JsonElement.toSaleItemOrNull(): SaleItem? {
    val obj = this as? JsonObject ?: return null
    val productId = listOf("productId", "product_id", "id", "\$id")
        .firstNotNullOfOrNull { key -> obj[key].stringOrNull()?.takeIf { it.isNotBlank() } }
        ?: return null
    val quantity = obj["quantity"].intOrNull() ?: 1
    val productName = listOf("productName", "product_name", "name", "title")
        .firstNotNullOfOrNull { key -> obj[key].stringOrNull()?.takeIf { it.isNotBlank() } }

    return SaleItem(
        productId = productId,
        quantity = quantity,
        productName = productName
    )
}

private fun JsonElement?.stringOrNull(): String? = (this as? JsonPrimitive)?.content

private fun JsonElement?.intOrNull(): Int? = (this as? JsonPrimitive)?.intOrNull

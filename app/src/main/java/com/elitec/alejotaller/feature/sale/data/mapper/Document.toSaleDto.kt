package com.elitec.alejotaller.feature.sale.data.mapper

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import io.appwrite.models.Document
import kotlinx.datetime.LocalDate

fun Document<Map<String, Any>>.toSaleDto(): SaleDto =
    SaleDto(
        id = id,
        date = data["date"].toLocalDate(),
        amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
        products = data["products"].toSaleItems(),
        verified =  data["verified"] as? String ?: "UNVERIFIED",
        userId = data["user_id"] as? String ?: ""
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
            is String -> SaleItem(productId = item, quantity = 1)
            is Map<*, *> -> {
                val productId = item["productId"] as? String ?: return@mapNotNull null
                val quantity = (item["quantity"] as? Number)?.toInt() ?: 1
                SaleItem(productId = productId, quantity = quantity)
            }
            else -> null
        }
    }

    else -> emptyList()
}
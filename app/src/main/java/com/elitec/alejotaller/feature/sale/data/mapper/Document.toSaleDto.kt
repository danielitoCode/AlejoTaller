package com.elitec.alejotaller.feature.sale.data.mapper

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import io.appwrite.models.Document
import kotlinx.datetime.LocalDate

fun Document<Map<String, Any>>.toSaleDto(): SaleDto =
    SaleDto(
        id = id,
        date = data["date"] as LocalDate,
        amount = data["amount"] as Double,
        products = data["products"] as List<String>,
        userId = data["user_id"] as String
    )
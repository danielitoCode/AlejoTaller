package com.elitec.alejotaller.feature.sale.data.mapper

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import io.appwrite.extensions.toJson
import kotlinx.serialization.json.Json

fun Sale.toDto(): SaleDto =
    SaleDto(
        id = id,
        date = date,
        amount = amount,
        products = listOf(),
        userId = userId
    )

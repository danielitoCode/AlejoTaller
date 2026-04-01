package com.elitec.shared.data.feature.sale.data.mapper

import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import kotlinx.serialization.json.Json

fun Sale.toDto(): SaleDto =
    SaleDto(
        id = id,
        date = date,
        verified = verified.toString(),
        amount = amount,
        products = products,
        userId = userId,
        customerName = customerName,
        deliveryType = deliveryType?.toString(),
        deliveryAddress = deliveryAddress?.let { Json.encodeToString(it) }
    )

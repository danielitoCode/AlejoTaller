package com.elitec.shared.data.feature.sale.data.mapper

import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.data.feature.sale.data.dto.toBuyState
import com.elitec.shared.data.feature.sale.data.dto.toDeliveryType
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryAddress
import kotlinx.serialization.json.Json

fun SaleDto.toDomain(): Sale =
    Sale(
        id = id,
        date = date,
        amount = amount,
        products = products,
        userId = userId,
        customerName = customerName,
        verified = verified.toBuyState(),
        deliveryType = deliveryType?.toDeliveryType(),
        deliveryAddress = deliveryAddress?.let {
            runCatching { Json.decodeFromString<DeliveryAddress>(it) }.getOrNull()
        }
    )



package com.elitec.alejotaller.feature.sale.data.mapper

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun Sale.toDto(): SaleDto =
    SaleDto(
        id = id,
        date = date,
        verified = verified.toString(),
        amount = amount,
        products = products,
        userId = userId
    )

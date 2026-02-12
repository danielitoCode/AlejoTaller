package com.elitec.alejotaller.feature.sale.data.mapper

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.Sale

fun Sale.toDto(): SaleDto =
    SaleDto(
        id = id,
        date = date,
        amount = amount,
        products = products.toListString(),
        userId = userId
    )

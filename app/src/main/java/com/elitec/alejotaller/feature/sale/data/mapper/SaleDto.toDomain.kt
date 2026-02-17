package com.elitec.alejotaller.feature.sale.data.mapper

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.data.dto.toBuyState
import com.elitec.alejotaller.feature.sale.domain.entity.Sale

fun SaleDto.toDomain(): Sale =
    Sale(
        id = id,
        date = date,
        amount = amount,
        products = products,
        userId = userId,
        verified = verified.toBuyState()
    )
package com.elitec.alejotaller.feature.sale.data.mapper

import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import kotlinx.serialization.json.Json

fun String.toSaleItemList(): List<SaleItem> {
    return Json.decodeFromString(this)
}
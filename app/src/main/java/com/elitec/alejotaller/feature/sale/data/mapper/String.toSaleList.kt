package com.elitec.shared.data.feature.sale.data.mapper

import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import kotlinx.serialization.json.Json

fun String.toSaleItemList(): List<SaleItem> {
    return Json.decodeFromString(this)
}
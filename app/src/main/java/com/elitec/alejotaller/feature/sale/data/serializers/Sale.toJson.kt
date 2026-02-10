package com.elitec.alejotaller.feature.sale.data.serializers

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun String.toSaleItemList(): List<SaleItem> = Json.decodeFromString(this)


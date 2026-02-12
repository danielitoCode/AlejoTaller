package com.elitec.alejotaller.feature.sale.data.mapper

import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import io.appwrite.extensions.toJson
import kotlinx.serialization.json.Json

fun String.toSaleItem(): SaleItem = Json.decodeFromString(this)

fun List<String>.toSaleItemList(): List<SaleItem> = this.map { item -> item.toSaleItem() }

fun List<SaleItem>.toListString(): List<String> = this.map { item -> item.toJson() }

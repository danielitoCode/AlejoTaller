package com.elitec.alejotaller.infraestructure.core.data.converters

import androidx.room.TypeConverter
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ListConverter {
    @TypeConverter
    fun fromList(value: List<String>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toList(value: String?): List<String>? = value?.let { Json.decodeFromString<List<String>>(it) }

    @TypeConverter
    fun fromSaleItemList(value: List<SaleItem>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toSaleItemList(value: String?): List<SaleItem>? = value?.let { Json.decodeFromString<List<SaleItem>>(it) }
}
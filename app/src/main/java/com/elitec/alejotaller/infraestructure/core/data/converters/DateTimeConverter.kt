package com.elitec.alejotaller.infraestructure.core.data.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime

class DateTimeConverter {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }
}
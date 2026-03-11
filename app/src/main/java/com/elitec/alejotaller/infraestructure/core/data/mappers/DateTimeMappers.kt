package com.elitec.alejotaller.infraestructure.core.data.mappers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun LocalTime.toJsonString(): String = Json.encodeToString(this)

fun String.toLocalDate(): LocalDate = Json.decodeFromString(this)

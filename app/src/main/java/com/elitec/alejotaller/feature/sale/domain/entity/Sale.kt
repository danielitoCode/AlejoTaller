package com.elitec.alejotaller.feature.sale.domain.entity

import com.elitec.alejotaller.infraestructure.core.domain.CoreEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Sale(
    override val id: String,
    val date: String,
    // val date: LocalDate,
    val amount: Double,
    val joyas: List<String>,
    @SerialName("user_id")
    val userId: String
): CoreEntity
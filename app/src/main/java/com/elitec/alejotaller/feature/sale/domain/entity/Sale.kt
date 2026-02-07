package com.elitec.alejotaller.feature.sale.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.elitec.alejotaller.infraestructure.core.domain.CoreEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate

@Entity
@Serializable
data class Sale(
    @PrimaryKey override val id: String,
    val date: LocalDate,
    val amount: Double,
    val products: List<String>,
    @SerialName("user_id")
    val userId: String
): CoreEntity
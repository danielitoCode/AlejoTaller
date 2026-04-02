package com.elitec.alejotallerscan.feature.history.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operator_sale_record")
data class OperatorSaleRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: String,
    val customerName: String?,
    val userId: String,
    val amount: Double,
    val saleDate: String,
    val action: String,
    val stateAfter: String,
    val recordedAtEpochMillis: Long,
    val itemsSummary: String
)

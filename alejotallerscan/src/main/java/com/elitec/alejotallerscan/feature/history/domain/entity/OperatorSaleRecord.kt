package com.elitec.alejotallerscan.feature.history.domain.entity

data class OperatorSaleRecord(
    val id: Long = 0,
    val saleId: String,
    val customerName: String?,
    val userId: String,
    val amount: Double,
    val saleDate: String,
    val action: OperatorSaleRecordAction,
    val stateAfter: String,
    val recordedAtEpochMillis: Long,
    val itemsSummary: List<String>
)

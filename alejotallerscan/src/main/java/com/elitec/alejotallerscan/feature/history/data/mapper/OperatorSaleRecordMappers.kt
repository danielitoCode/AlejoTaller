package com.elitec.alejotallerscan.feature.history.data.mapper

import com.elitec.alejotallerscan.feature.history.data.local.entity.OperatorSaleRecordEntity
import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecord
import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecordAction
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

fun OperatorSaleRecordEntity.toDomain(): OperatorSaleRecord =
    OperatorSaleRecord(
        id = id,
        saleId = saleId,
        customerName = customerName,
        userId = userId,
        amount = amount,
        saleDate = saleDate,
        action = OperatorSaleRecordAction.valueOf(action),
        stateAfter = stateAfter,
        recordedAtEpochMillis = recordedAtEpochMillis,
        itemsSummary = itemsSummary.split('\n').filter { it.isNotBlank() }
    )

fun Sale.toOperatorRecordEntity(action: OperatorSaleRecordAction, recordedAtEpochMillis: Long): OperatorSaleRecordEntity =
    OperatorSaleRecordEntity(
        saleId = id,
        customerName = customerName,
        userId = userId,
        amount = amount,
        saleDate = date.toString(),
        action = action.name,
        stateAfter = verified.name,
        recordedAtEpochMillis = recordedAtEpochMillis,
        itemsSummary = products.joinToString(separator = "\n") { item ->
            "${item.productName ?: item.productId} x${item.quantity}"
        }
    )

package com.elitec.alejotallerscan.feature.history.domain.repository

import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecord
import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecordAction
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import kotlinx.coroutines.flow.Flow

interface OperatorSaleRecordRepository {
    fun observeAll(): Flow<List<OperatorSaleRecord>>
    suspend fun register(sale: Sale, action: OperatorSaleRecordAction)
}

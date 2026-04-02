package com.elitec.alejotallerscan.feature.history.data.repository

import com.elitec.alejotallerscan.feature.history.data.local.dao.OperatorSaleRecordDao
import com.elitec.alejotallerscan.feature.history.data.mapper.toDomain
import com.elitec.alejotallerscan.feature.history.data.mapper.toOperatorRecordEntity
import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecordAction
import com.elitec.alejotallerscan.feature.history.domain.repository.OperatorSaleRecordRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomOperatorSaleRecordRepository(
    private val dao: OperatorSaleRecordDao
) : OperatorSaleRecordRepository {
    override fun observeAll(): Flow<List<com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecord>> =
        dao.observeAll().map { records -> records.map { it.toDomain() } }

    override suspend fun register(sale: Sale, action: OperatorSaleRecordAction) {
        dao.insert(
            sale.toOperatorRecordEntity(
                action = action,
                recordedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }
}

package com.elitec.alejotallerscan.feature.history.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elitec.alejotallerscan.feature.history.data.local.entity.OperatorSaleRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperatorSaleRecordDao {
    @Query("SELECT * FROM operator_sale_record ORDER BY recordedAtEpochMillis DESC")
    fun observeAll(): Flow<List<OperatorSaleRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: OperatorSaleRecordEntity)
}

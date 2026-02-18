package com.elitec.alejotaller.feature.notifications.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elitec.alejotaller.feature.notifications.data.dto.PromotionDto
import kotlinx.coroutines.flow.Flow

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotions WHERE validUntilEpochMillis >= :now ORDER BY validUntilEpochMillis ASC")
    fun observeActive(now: Long): Flow<List<PromotionDto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PromotionDto)

    @Query("DELETE FROM promotions WHERE validUntilEpochMillis < :now")
    suspend fun clearExpired(now: Long)
}
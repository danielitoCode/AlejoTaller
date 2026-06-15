package com.elitec.alejotaller.feature.exchange.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elitec.alejotaller.feature.exchange.data.dto.CupExchangeLocalDto

@Dao
interface ExchangeDao {
    @Query("SELECT * FROM exchange_rates WHERE id = :id")
    suspend fun getExchangeById(id: String): CupExchangeLocalDto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchange(exchange: CupExchangeLocalDto)
}

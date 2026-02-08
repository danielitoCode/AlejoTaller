package com.elitec.alejotaller.feature.sale.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elitec.alejotaller.feature.sale.data.dto.SaleDto

@Dao
interface SaleDao {

    @Query("SELECT * FROM saledto")
    fun getAll(): List<SaleDto>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(items: List<SaleDto>)

    @Transaction
    suspend fun replaceAll(joyas: List<SaleDto>) {
        deleteAll()
        insertAll(joyas)
    }

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(items: SaleDto)

    @Query("SELECT * FROM saledto WHERE id = :id")
    suspend fun getById(id: String): SaleDto

    @Query("DELETE FROM saledto WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM saledto")
    suspend fun deleteAll()
}

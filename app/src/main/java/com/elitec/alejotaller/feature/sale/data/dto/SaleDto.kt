package com.elitec.alejotaller.feature.sale.data.dto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elitec.alejotaller.feature.sale.domain.entity.Sale

@Dao
interface SaleDto {

    @Query("SELECT * FROM sale")
    fun getAll(): List<Sale>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Sale>)

    @Transaction
    suspend fun replaceAll(joyas: List<Sale>) {
        deleteAll()
        insertAll(joyas)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: Sale)

    @Query("SELECT * FROM sale WHERE id = :id")
    suspend fun getById(id: String): Sale

    @Query("DELETE FROM sale WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM sale")
    suspend fun deleteAll()
}
package com.elitec.alejotaller.feature.product.data.dto

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elitec.alejotaller.feature.product.domain.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM product")
    fun getAllFlow(): Flow<List<Product>>

    @Query("SELECT * FROM product")
    fun getAll(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Product>)

    @Transaction
    suspend fun replaceAll(products: List<Product>) {
        deleteAll()
        insertAll(products)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: Product)

    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getById(id: String): Product

    @Query("DELETE FROM product WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM product")
    suspend fun deleteAll()
}
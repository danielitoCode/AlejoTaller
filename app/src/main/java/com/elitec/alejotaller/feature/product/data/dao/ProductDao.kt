package com.elitec.alejotaller.feature.product.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.domain.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM productdto")
    fun getAllFlow(): Flow<List<ProductDto>>

    @Query("SELECT * FROM productdto")
    fun getAll(): List<ProductDto>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(items: List<ProductDto>)

    @Transaction
    suspend fun replaceAll(products: List<ProductDto>) {
        deleteAll()
        insertAll(products)
    }

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(items: ProductDto)

    @Query("SELECT * FROM productdto WHERE id = :id")
    suspend fun getById(id: String): ProductDto

    @Query("DELETE FROM productdto WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM productdto")
    suspend fun deleteAll()
}
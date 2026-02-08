package com.elitec.alejotaller.feature.category.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.category.domain.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categorydto")
    fun observeAll(): Flow<List<CategoryDto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CategoryDto>)

    @Transaction
    suspend fun replaceAll(categories: List<CategoryDto>) {
        deleteAll()
        insertAll(categories)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: CategoryDto)

    @Query("SELECT * FROM categorydto WHERE id = :id")
    suspend fun getById(id: String): CategoryDto?

    @Query("DELETE FROM categorydto WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM categorydto")
    suspend fun deleteAll()
}
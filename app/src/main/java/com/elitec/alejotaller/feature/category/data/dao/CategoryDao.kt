package com.elitec.alejotaller.feature.category.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elitec.alejotaller.feature.category.domain.entity.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    fun getAll(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Category>)

    @Transaction
    suspend fun replaceAll(categories: List<Category>) {
        deleteAll()
        insertAll(categories)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: Category)

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getById(id: String): Category

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM category")
    suspend fun deleteAll()
}
package com.elitec.alejotaller.feature.category.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.elitec.alejotaller.feature.category.data.dao.CategoryDao
import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class CategoriesOfflineFirstRepositoryTest {
    private lateinit var  volatileDb: AppBD
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        volatileDb = Room.inMemoryDatabaseBuilder(
            context,
            AppBD::class.java
        ).build()
        categoryDao = volatileDb.categoriesDao()
    }

    @After
    fun tearDown() {
        volatileDb.close()
    }

    @Test
    fun insert_a_massive_list_categories_and_query_returns_in_expected_order() = runTest {
        // Given
        val categoriesList = listOf(
            CategoryDto("category1", "Category test 1", "photo url 1"),
            CategoryDto("category2", "Category test 2", "photo url 2"),
            CategoryDto("category3", "Category test 3", "photo url 3")
        )

        // When
        categoryDao.insertAll(categoriesList)

        // Then
        val response = categoryDao.observeAll().first()
        assertEquals(categoriesList.sortedByDescending { it.id }, response)
    }

    @Test
    fun insert_and_query_categories_returns_in_expected_order() = runTest {
        // Given
        val category1 = CategoryDto("category1", "Category test 1", "photo url 1")
        val category2 = CategoryDto("category2", "Category test 2", "photo url 2")
        val category3 = CategoryDto("category3", "Category test 3", "photo url 3")

        // When
        categoryDao.insert(category1)
        categoryDao.insert(category2)
        categoryDao.insert(category3)

        // Then
        val response = categoryDao.observeAll().first()
        assertEquals(
            listOf(
                category1,
                category2,
                category3
            ).sortedByDescending { it.id },
            response
        )
    }

    @Test
    fun get_not_existing_category_by_id() = runTest {
        // Given
        val categoriesList = listOf(
            CategoryDto("category1", "Category test 1", "photo url 1"),
            CategoryDto("category2", "Category test 2", "photo url 2"),
            CategoryDto("category3", "Category test 3", "photo url 3")
        )
        // When
        categoryDao.insertAll(categoriesList)
        // Then
        val response = categoryDao.getById("category4")

        assertEquals(null, response)
    }

    @Test
    fun get_existing_category_by_id() = runTest {
        // Given
        val categoriesList = listOf(
            CategoryDto("category1", "Category test 1", "photo url 1"),
            CategoryDto("category2", "Category test 2", "photo url 2"),
            CategoryDto("category3", "Category test 3", "photo url 3")
        )
        // When
        categoryDao.insertAll(categoriesList)
        // Then
        val response = categoryDao.getById("category2")

        assertEquals(
            CategoryDto(
                "category2",
                "Category test 2",
                "photo url 2"),
            response
        )
    }
}
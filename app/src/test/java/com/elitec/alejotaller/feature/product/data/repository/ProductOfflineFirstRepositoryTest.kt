package com.elitec.alejotaller.feature.product.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.elitec.alejotaller.R
import com.elitec.alejotaller.core.MainDispatcherRule
import com.elitec.alejotaller.core.createTestDatabase
import com.elitec.alejotaller.fakes.FakeProductNetRepository
import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.product.data.dao.ProductDao
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.data.test.productTestList
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import io.appwrite.services.Databases
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductOfflineFirstRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private lateinit var db: AppBD
    private lateinit var dao: ProductDao
    private lateinit var net: FakeProductNetRepository
    private lateinit var repository: ProductOfflineFirstRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = createTestDatabase(context)
        dao = db.productsDao()

        net = FakeProductNetRepository()

        repository = ProductOfflineFirstRepository(
            net,
            dao
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun sync_downloads_and_caches_products() = runTest {

        repository.sync()

        val cached = dao.getAll()

        assertEquals(2, cached.size)
    }
}
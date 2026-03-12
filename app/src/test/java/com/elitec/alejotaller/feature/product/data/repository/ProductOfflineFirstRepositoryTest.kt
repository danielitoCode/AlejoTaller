package com.elitec.alejotaller.feature.product.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.elitec.alejotaller.core.MainDispatcherRule
import com.elitec.alejotaller.core.createTestDatabase
import com.elitec.alejotaller.data.fakesRepositories.FakeProductNetRepository
import com.elitec.alejotaller.feature.product.data.dao.ProductDao
import com.elitec.alejotaller.infraestructure.core.data.bd.AppBD
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

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
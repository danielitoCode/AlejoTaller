package com.elitec.alejotaller.feature.product.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
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
    fun sync_downloads_and_caches_products_ordered_by_descending() = runTest {

        repository.sync()

        val cached = repository.observeAll().first()

        assertEquals(5, cached.size)
    }

    @Test
    fun get_product_by_id () = runTest {
        repository.sync()

        val cached = repository.observeAll().first()
        val selectedProduct = cached[Random.nextInt(0,cached.size-1)]

        assertEquals(selectedProduct.name,repository.getById(selectedProduct.id)?.name)
    }

    @Test
    fun get_a_not_exist_id_in_repository() = runTest {
        repository.sync()

        assertEquals(null,repository.getById("Id not saved"))
    }
}
package com.elitec.alejotaller.feature.product.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.elitec.alejotaller.R
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
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductOfflineFirstRepositoryTest {
    private lateinit var  volatileDb: AppBD

    private lateinit var productOfflineRepo: ProductOfflineFirstRepository
    private lateinit var dao: ProductDao
    private val listProductDtoTest = listOf(
        ProductDto(
            id = "1",
            name = "Baterias de litio LiFPo4 3.2Ah",
            description = "Baterias de litio a 3.25 AH,perfecta para reparacion de unidades de acumulacion en mal estado o el montaje personalizado de sistemas de respaldo de alta capacidad",
            price = 45.50,
            photoUrl = "https://example.com/photos/aceite.jpg",
            categoryId = "baterias",
            photoLocalResource = R.drawable.li3_2a
        ),
        ProductDto(
            id = "2",
            name = "BMS 5v",
            description = "BMS para baterias de 5v, dele a su bateria la energia necesaria, ni mas ni menos",
            price = 18.25,
            photoUrl = "https://example.com/photos/filtro_aire.jpg",
            categoryId = "bms",
            photoLocalResource = R.drawable.bms5v
        ),
        ProductDto(
            id = "3",
            name = "EchoFlow delta max",
            description = "Unidad de respaldo inteligente de alta eficiencia, bateria LifPo4 con una capacidad de carga de 2800Wh , y un pico de 3600W",
            price = 65.00,
            photoUrl = "https://example.com/photos/pastillas_freno.jpg",
            categoryId = "equipos",
            photoLocalResource = R.drawable.echoflow_deltamax
        ),
        ProductDto(
            id = "4",
            name = "Batería LiOn 3.2v 1Ah",
            description = "Bateria hecha para sus dispositivos que ocupen poco espacio o espacio reducido, con una capacidad suficiente para uso promedio",
            price = 110.99,
            photoUrl = "https://example.com/photos/bateria.jpg",
            categoryId = "baterias",
            photoLocalResource = R.drawable.li1a
        ),
        ProductDto(
            id = "5",
            name = "Transistor 2N3904",
            description = "Transistos NPN, para uso promedio",
            price = 12.50,
            photoUrl = "https://example.com/photos/bujia.jpg",
            categoryId = "componentes",
            photoLocalResource = R.drawable.t2n3904
        ),
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        volatileDb = Room.inMemoryDatabaseBuilder(
            context,
            AppBD::class.java
        ).build()
        dao = volatileDb.productsDao()
    }

    @After
    fun tearDown() {
        volatileDb.close()
    }

    @Test
    fun insert_a_massive_list_products_and_query_returns_in_expected_order() = runTest {
        // When
        dao.insertAll(listProductDtoTest)

        // Then
        val response = productOfflineRepo.observeAll().first()
        val expectedData = listProductDtoTest
            .map { productDto ->
                Product(
                    productDto.id,
                    productDto.name,
                    productDto.description,
                    productDto.price,
                    productDto.photoUrl,
                    productDto.categoryId,
                    productDto.rating
                )
            }
            .sortedByDescending { item -> item.id }
        assertEquals(expectedData, response)
    }

    @Test
    fun insert_and_query_products_returns_in_expected_order() = runTest {
        // Given
        val product1 = ProductDto(
            id = "p1",
            name = "Product test 1",
            description = "producto 1 description",
            price = 1.0,
            photoUrl = "product photo url 1",
            categoryId = "category 1",
            rating = 4.2
        )
        val product2 = ProductDto(
            id = "p2",
            name = "Product test 2",
            description = "product 2 description",
            price = 3.0,
            photoUrl = "product photo url 2",
            categoryId = "category 1",
            rating = 4.0
        )
        val product3 = ProductDto(
            id = "p3",
            name = "Product test 3",
            description = "product 3 description",
            price = 2.0,
            photoUrl = "product photo url 3",
            categoryId = "category 2",
            rating = 4.4
        )

        // When
        dao.insert(product1)
        dao.insert(product2)
        dao.insert(product3)

        // Then
        val response = dao.getAllFlow().first()
        assertEquals(
            listOf(
                product1,
                product2,
                product3
            ).sortedByDescending { it.id },
            response
        )
    }

    @Test
    fun get_not_existing_product_by_id() = runTest {
        // When
        dao.insertAll(listProductDtoTest)
        // Then
        val response = dao.getById("7")

        assertNull(response)
    }

    @Test
    fun get_existing_product_by_id() = runTest {
        // When
        dao.insertAll(listProductDtoTest)
        // Then
        val response = dao.getById("4")

        assertEquals(
            listProductDtoTest.first { it.id == "4" },
            response
        )
    }
}
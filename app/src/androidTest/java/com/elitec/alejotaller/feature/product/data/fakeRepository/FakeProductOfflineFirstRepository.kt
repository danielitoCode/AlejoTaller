package com.elitec.alejotaller.feature.product.data.fakeRepository

import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.data.mapper.toDomain
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update

class FakeProductOfflineFirstRepository: ProductRepository {
    private val fakeProductFlow = MutableStateFlow(listOf<Product>())

    private val fakeProducts = listOf(
        ProductDto(
            id = "1",
            name = "Baterias de litio LiFPo4 3.2Ah",
            description = "Baterias de litio a 3.25 AH,perfecta para reparacion de unidades de acumulacion en mal estado o el montaje personalizado de sistemas de respaldo de alta capacidad",
            price = 45.50,
            photoUrl = "https://example.com/photos/aceite.jpg",
            categoryId = "baterias",
            photoLocalResource = R.drawable.li3_2a,
            existence = 10,
            reserved = 0
        ),
        ProductDto(
            id = "2",
            name = "BMS 5v",
            description = "BMS para baterias de 5v, dele a su bateria la energia necesaria, ni mas ni menos",
            price = 18.25,
            photoUrl = "https://example.com/photos/filtro_aire.jpg",
            categoryId = "bms",
            photoLocalResource = R.drawable.bms5v,
            existence = 5,
            reserved = 0
        ),
        ProductDto(
            id = "3",
            name = "EchoFlow delta max",
            description = "Unidad de respaldo inteligente de alta eficiencia, bateria LifPo4 con una capacidad de carga de 2800Wh , y un pico de 3600W",
            price = 65.00,
            photoUrl = "https://example.com/photos/pastillas_freno.jpg",
            categoryId = "equipos",
            photoLocalResource = R.drawable.echoflow_deltamax,
            existence = 3,
            reserved = 0
        ),
        ProductDto(
            id = "4",
            name = "Batería LiOn 3.2v 1Ah",
            description = "Bateria hecha para sus dispositivos que ocupen poco espacio o espacio reducido, con una capacidad suficiente para uso promedio",
            price = 110.99,
            photoUrl = "https://example.com/photos/bateria.jpg",
            categoryId = "baterias",
            photoLocalResource = R.drawable.li1a,
            existence = 20,
            reserved = 0
        ),
        ProductDto(
            id = "5",
            name = "Transistor 2N3904",
            description = "Transistos NPN, para uso promedio",
            price = 12.50,
            photoUrl = "https://example.com/photos/bujia.jpg",
            categoryId = "componentes",
            photoLocalResource = R.drawable.t2n3904,
            existence = 100,
            reserved = 0
        ),
    ).map { productDto -> productDto.toDomain() }

    override fun observeAll(): Flow<List<Product>> = fakeProductFlow.asStateFlow()

    override suspend fun getById(itemId: String): Product?  {
        val fakeCategoryListen = fakeProductFlow.last()
        return fakeCategoryListen.firstOrNull { category -> category.id == itemId }
    }

    override suspend fun sync(): Result<Unit> = runCatching {
        fakeProducts.forEachIndexed { index, product ->
            fakeProductFlow.update { list ->
                val tempCategoryList = list.toMutableList()
                tempCategoryList.add(product)
                tempCategoryList
            }
            delay(400L * index)
        }
    }

    override suspend fun incrementReserved(productId: String, quantity: Int): Product? {
        var updated: Product? = null
        fakeProductFlow.update { list ->
            list.map { p ->
                if (p.id == productId) {
                    p.copy(reserved = (p.reserved + quantity).coerceAtLeast(0)).also { updated = it }
                } else p
            }
        }
        return updated ?: getById(productId)
    }
}

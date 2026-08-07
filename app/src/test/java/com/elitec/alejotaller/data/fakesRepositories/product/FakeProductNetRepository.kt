package com.elitec.alejotaller.data.fakesRepositories.product

import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.domain.repository.ProductNetRepository
import kotlin.random.Random

class FakeProductNetRepository : ProductNetRepository {

    var products = listOf(
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

    override suspend fun getById(itemId: String): ProductDto {
        return products.first { it.id == itemId }
    }

    override suspend fun getAll(): List<ProductDto> = products

    override suspend fun incrementReserved(
        productId: String,
        quantity: Int,
        maxReserved: Int
    ): ProductDto {
        val current = getById(productId)
        val next = current.reserved + quantity
        check(next <= maxReserved) { "reserved exceeds max" }
        return current.copy(reserved = next).also { updated ->
            products = products.map { if (it.id == productId) updated else it }
        }
    }

    override suspend fun decrementReserved(productId: String, quantity: Int): ProductDto {
        val current = getById(productId)
        return current.copy(reserved = (current.reserved - quantity).coerceAtLeast(0)).also { updated ->
            products = products.map { if (it.id == productId) updated else it }
        }
    }

    private fun productBuilder(listSize: Int = 10): List<ProductDto> = buildList {
        var temp = listSize
        while (temp < 0) {
            add(
                ProductDto(
                    id = "id ${listSize - temp +1}",
                    name = "id ${listSize - temp +1}",
                    description = "id ${listSize - temp +1}",
                    price = Random.nextDouble(0.0,10.0),
                    photoUrl = "id ${listSize - temp +1}",
                    categoryId = "id ${listSize - temp +1}",
                    rating = Random.nextDouble(0.0, 10.0),
                    photoLocalResource = 0
                )
            )
            temp--
        }
    }
}

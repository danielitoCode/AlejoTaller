package com.elitec.alejotaller.feature.product.data.test

import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.domain.entity.Product

val productTestList = listOf(
    Product(
        id = "1",
        name = "Baterias de litio LiFPo4 3.2Ah",
        description = "Baterias de litio a 3.25 AH,perfecta para reparacion de unidades de acumulacion en mal estado o el montaje personalizado de sistemas de respaldo de alta capacidad",
        price = 45.50,
        photoUrl = "https://example.com/photos/aceite.jpg",
        categoryId = "baterias",
        photoLocalResource = R.drawable.li3_2a
    ),
    Product(
        id = "2",
        name = "BMS 5v",
        description = "BMS para baterias de 5v, dele a su bateria la energia necesaria, ni mas ni menos",
        price = 18.25,
        photoUrl = "https://example.com/photos/filtro_aire.jpg",
        categoryId = "bms",
        photoLocalResource = R.drawable.bms5v
    ),
    Product(
        id = "3",
        name = "EchoFlow delta max",
        description = "Unidad de respaldo inteligente de alta eficiencia, bateria LifPo4 con una capacidad de carga de 2800Wh , y un pico de 3600W",
        price = 65.00,
        photoUrl = "https://example.com/photos/pastillas_freno.jpg",
        categoryId = "equipos",
        photoLocalResource = R.drawable.echoflow_deltamax
    ),
    Product(
        id = "4",
        name = "Batería LiOn 3.2v 1Ah",
        description = "Bateria hecha para sus dispositivos que ocupen poco espacio o espacio reducido, con una capacidad suficiente para uso promedio",
        price = 110.99,
        photoUrl = "https://example.com/photos/bateria.jpg",
        categoryId = "baterias",
        photoLocalResource = R.drawable.li1a
    ),
    Product(
        id = "5",
        name = "Transistor 2N3904",
        description = "Transistos NPN, para uso promedio",
        price = 12.50,
        photoUrl = "https://example.com/photos/bujia.jpg",
        categoryId = "componentes",
        photoLocalResource = R.drawable.t2n3904
    ),
)
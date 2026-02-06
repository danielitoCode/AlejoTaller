package com.elitec.alejotaller.feature.product.data.test

import com.elitec.alejotaller.feature.product.domain.entity.Product

val productTestList = listOf(
    Product(
        id = "1",
        name = "Aceite de Motor 10W-40",
        description = "Aceite sintético de alta calidad para motores a gasolina, botella de 1L.",
        price = 45.50,
        photoUrl = "https://example.com/photos/aceite.jpg",
        categoriaId = "cat_lubricantes"
    ),
    Product(
        id = "2",
        name = "Filtro de Aire Premium",
        description = "Filtro de aire de alto flujo para mejorar el rendimiento del motor.",
        price = 18.25,
        photoUrl = "https://example.com/photos/filtro_aire.jpg",
        categoriaId = "cat_repuestos"
    ),
    Product(
        id = "3",
        name = "Pastillas de Freno Cerámicas",
        description = "Juego de pastillas delanteras para mayor durabilidad y menor ruido.",
        price = 65.00,
        photoUrl = "https://example.com/photos/pastillas_freno.jpg",
        categoriaId = "cat_frenos"
    ),
    Product(
        id = "4",
        name = "Batería 12V 75Ah",
        description = "Batería de libre mantenimiento con alta capacidad de arranque en frío.",
        price = 110.99,
        photoUrl = "https://example.com/photos/bateria.jpg",
        categoriaId = "cat_electrico"
    ),
    Product(
        id = "5",
        name = "Bujía de Iridio",
        description = "Bujía de alto rendimiento para una combustión más eficiente.",
        price = 12.50,
        photoUrl = "https://example.com/photos/bujia.jpg",
        categoriaId = "cat_encendido"
    )
)
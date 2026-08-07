package com.elitec.alejotaller.feature.product.data.mapper

import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import io.appwrite.models.Document

private fun Map<String, Any>.readNonNegInt(vararg keys: String): Int {
    for (key in keys) {
        val raw = this[key] ?: continue
        val n = when (raw) {
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull()
            else -> null
        }
        if (n != null) return n.coerceAtLeast(0)
    }
    return 0
}

fun Document<Map<String, Any>>.toProductDto(): ProductDto =
    ProductDto(
        id = this.id,
        name = (data["name"] as? String) ?: "Sin nombre",
        photoUrl = (data["photo_url"] as? String) ?: "",
        description = (data["description"] as? String) ?: "",
        price = (data["price"] as? Number)?.toDouble() ?: 0.0,
        categoryId = (data["category_id"] as? String) ?: "",
        rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
        photoLocalResource = 1,
        // Schema real Appwrite: stock físico = `status` (label consola "Estado")
        // Policy domain: existence. También acepta existence si se migra el atributo.
        existence = data.readNonNegInt("existence", "status", "Estado", "stock", "cantidad"),
        reserved = data.readNonNegInt("reserved", "reservado")
    )

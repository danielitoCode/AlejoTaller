package com.elitec.alejotaller.feature.product.data.mapper

import com.elitec.alejotaller.feature.product.data.dto.ProductDto

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

/** Mapea payload Appwrite Realtime (Map) a ProductDto para cache local. */
fun Map<String, Any>.toProductDtoFromRealtime(): ProductDto? {
    val id = (this["\$id"] as? String)?.trim()
        ?: (this["id"] as? String)?.trim()
        ?: return null
    if (id.isEmpty()) return null

    return ProductDto(
        id = id,
        name = (this["name"] as? String) ?: "Sin nombre",
        photoUrl = (this["photo_url"] as? String) ?: "",
        description = (this["description"] as? String) ?: "",
        price = (this["price"] as? Number)?.toDouble() ?: 0.0,
        categoryId = (this["category_id"] as? String) ?: "",
        rating = (this["rating"] as? Number)?.toDouble() ?: 0.0,
        photoLocalResource = 1,
        existence = readNonNegInt("existence", "status", "Estado", "stock", "cantidad"),
        reserved = readNonNegInt("reserved", "reservado")
    )
}

package com.elitec.alejotaller.feature.product.domain.ports

import kotlinx.coroutines.flow.StateFlow

/**
 * Likes de productos solo en dispositivo (paridad web localStorage).
 * Clave de persistencia alineada: [STORAGE_KEY].
 * No toca backend ni entidad Product.
 */
interface LikedProductsStore {
    val likedIds: StateFlow<Set<String>>

    fun isLiked(productId: String): Boolean

    /** @return true si queda likeado tras el toggle */
    fun toggle(productId: String): Boolean

    fun like(productId: String)

    fun unlike(productId: String)

    companion object {
        /** Misma clave conceptual que web: alejo_liked_product_ids */
        const val STORAGE_KEY = "alejo_liked_product_ids"
    }
}

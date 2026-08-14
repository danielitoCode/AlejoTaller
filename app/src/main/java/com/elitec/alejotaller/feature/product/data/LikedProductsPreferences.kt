package com.elitec.alejotaller.feature.product.data

import android.content.Context
import com.elitec.alejotaller.feature.product.domain.ports.LikedProductsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persistencia local de ids likeados.
 * Usa SharedPreferences StringSet bajo [LikedProductsStore.STORAGE_KEY].
 */
class LikedProductsPreferences(
    context: Context
) : LikedProductsStore {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _likedIds = MutableStateFlow(readIds())
    override val likedIds: StateFlow<Set<String>> = _likedIds.asStateFlow()

    override fun isLiked(productId: String): Boolean {
        val id = productId.trim()
        if (id.isEmpty()) return false
        return id in _likedIds.value
    }

    override fun toggle(productId: String): Boolean {
        val id = productId.trim()
        if (id.isEmpty()) return false
        val next = _likedIds.value.toMutableSet()
        val nowLiked = if (id in next) {
            next.remove(id)
            false
        } else {
            next.add(id)
            true
        }
        writeIds(next)
        _likedIds.value = next
        return nowLiked
    }

    override fun like(productId: String) {
        val id = productId.trim()
        if (id.isEmpty()) return
        if (id in _likedIds.value) return
        val next = _likedIds.value + id
        writeIds(next)
        _likedIds.value = next
    }

    override fun unlike(productId: String) {
        val id = productId.trim()
        if (id.isEmpty()) return
        if (id !in _likedIds.value) return
        val next = _likedIds.value - id
        writeIds(next)
        _likedIds.value = next
    }

    private fun readIds(): Set<String> {
        // getStringSet puede devolver set mutable compartido: copiar siempre
        val raw = prefs.getStringSet(LikedProductsStore.STORAGE_KEY, emptySet()) ?: emptySet()
        return raw.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    private fun writeIds(ids: Set<String>) {
        prefs.edit()
            .putStringSet(LikedProductsStore.STORAGE_KEY, HashSet(ids))
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "alejo_likes_prefs"
    }
}

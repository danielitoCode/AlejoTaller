package com.elitec.alejotaller.feature.product.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.entity.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    observeProductsCaseUse: ObserveProductsCaseUse,
    private val syncProductCaseUse: SyncProductCaseUse,
    private val getProductByIdCaseUse: GetProductByIdCaseUse
): ViewModel() {

    // ── Fuente de verdad: todos los productos crudos ──────────────────────
    private val _allProducts = observeProductsCaseUse().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    // ── Estado del buscador ───────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()


    // ── Categoría seleccionada (null = todas) ─────────────────────────────
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    // ── Productos ya filtrados (búsqueda + categoría) ──────────────────────
    val productFlow = combine(
        _allProducts,
        _searchQuery,
        _selectedCategoryId
    ) { products, query, categoryId ->
        products
            .filter { product ->
                // Filtro por categoría: si no hay selección, pasan todos
                categoryId == null || product.categoryId == categoryId
            }
            .filter { product ->
                // Filtro por texto: busca en nombre y descripción, sin importar mayúsculas
                if (query.isBlank()) true
                else product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true)
            }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    // ── Eventos del usuario ───────────────────────────────────────────────
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    fun onCategorySelected(categoryId: String?) {
        // Si se selecciona la misma categoría, se deselecciona (toggle)
        _selectedCategoryId.value =
            if (_selectedCategoryId.value == categoryId) null else categoryId
    }
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategoryId.value = null
    }

    fun syncProducts(onProductCharge: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            syncProductCaseUse()
                .onSuccess { onProductCharge() }
                .onFailure { onFail() }
        }
    }

    fun getProductById(id: String,onProductCharge: (Product?) -> Unit) {
        viewModelScope.launch {
            getProductByIdCaseUse(id)
                .onSuccess { product ->
                    onProductCharge(product)
                }
                .onFailure {
                    onProductCharge(null)
                }
        }
    }
}
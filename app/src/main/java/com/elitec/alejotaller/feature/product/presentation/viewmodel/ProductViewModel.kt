package com.elitec.alejotaller.feature.product.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.product.domain.caseUse.GetProductByIdCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.ObserveProductsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.RefreshProductsByIdsCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.SyncProductCaseUse
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.realtime.StockChangedPayload
import com.elitec.alejotaller.feature.product.domain.realtime.StockUpdatesListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Catálogo offline-first.
 * stock:changed → RefreshProductsByIds (Appwrite → Room) sin sync completo.
 * available = existence - reserved (WAREHOUSE_POLICY).
 */
class ProductViewModel(
    observeProductsCaseUse: ObserveProductsCaseUse,
    private val syncProductCaseUse: SyncProductCaseUse,
    private val getProductByIdCaseUse: GetProductByIdCaseUse,
    private val refreshProductsByIdsCaseUse: RefreshProductsByIdsCaseUse,
    private val stockUpdatesListener: StockUpdatesListener
) : ViewModel() {

    private val _allProducts = observeProductsCaseUse().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    /** true mientras se refrescan productos por señal stock:changed */
    private val _stockSyncing = MutableStateFlow(false)
    val stockSyncing = _stockSyncing.asStateFlow()

    private val _stockSyncMessage = MutableStateFlow<String?>(null)
    val stockSyncMessage = _stockSyncMessage.asStateFlow()

    private var stockUnsubscribe: (() -> Unit)? = null

    val productFlow = combine(
        _allProducts,
        _searchQuery,
        _selectedCategoryId
    ) { products, query, categoryId ->
        products
            .filter { product ->
                categoryId == null || product.categoryId == categoryId
            }
            .filter { product ->
                if (query.isBlank()) true
                else product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
            }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        startStockRealtime()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(categoryId: String?) {
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

    fun getProductById(id: String, onProductCharge: (Product?) -> Unit) {
        viewModelScope.launch {
            getProductByIdCaseUse(id)
                .onSuccess { product -> onProductCharge(product) }
                .onFailure { onProductCharge(null) }
        }
    }

    /** Arranca listener stock:changed (idempotente). */
    fun startStockRealtime() {
        if (stockUnsubscribe != null) {
            Log.i(TAG, "event=stock_listener_already_active")
            return
        }
        Log.i(TAG, "event=stock_listener_start")
        stockUnsubscribe = stockUpdatesListener.start { payload ->
            onStockChanged(payload)
        }
    }

    fun stopStockRealtime() {
        stockUnsubscribe?.invoke()
        stockUnsubscribe = null
        Log.i(TAG, "event=stock_listener_stop")
    }

    private fun onStockChanged(payload: StockChangedPayload) {
        Log.i(
            TAG,
            "event=stock_changed_handle reason=${payload.reason} " +
                "ids=${payload.productIds.joinToString(",")} saleId=${payload.saleId}"
        )
        viewModelScope.launch {
            _stockSyncing.value = true
            _stockSyncMessage.value = reasonLabel(payload.reason)
            refreshProductsByIdsCaseUse(payload.productIds)
                .onSuccess { updated ->
                    Log.i(
                        TAG,
                        "event=stock_refresh_ok count=${updated.size} " +
                            "ids=${updated.joinToString(",") { "${it.id}:ex=${it.existence}:rs=${it.reserved}" }}"
                    )
                }
                .onFailure { error ->
                    Log.w(TAG, "event=stock_refresh_fail cause=${error.message}", error)
                }
            _stockSyncing.value = false
            _stockSyncMessage.value = null
        }
    }

    private fun reasonLabel(reason: String): String = when (reason) {
        "hold" -> "Actualizando reservas…"
        "release" -> "Liberando stock…"
        "consume" -> "Confirmando venta…"
        else -> "Actualizando inventario…"
    }

    override fun onCleared() {
        stopStockRealtime()
        super.onCleared()
    }

    companion object {
        private const val TAG = "ProductViewModel"
    }
}

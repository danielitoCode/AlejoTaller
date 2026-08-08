package com.elitec.alejotallerscan.feature.sale.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotallerscan.feature.confirmation.domain.caseuse.NotifyOperatorSaleDecisionCaseUse
import com.elitec.alejotallerscan.feature.history.domain.caseuse.RegisterOperatorSaleRecordCaseUse
import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecordAction
import com.elitec.alejotallerscan.feature.product.domain.caseuse.ApplyOperatorStockDecisionCaseUse
import com.elitec.shared.data.feature.sale.data.dao.SaleDao
import com.elitec.shared.data.feature.sale.data.mapper.toDomain
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.sale.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateSaleVerificationFromRealtimeCaseUse
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OperatorSalesViewModel(
    private val observeAllSalesCaseUse: ObserveAllSalesCaseUse,
    private val saleNetRepository: SaleNetRepository,
    private val saleDao: SaleDao,
    private val updateSaleVerificationFromRealtimeCaseUse: UpdateSaleVerificationFromRealtimeCaseUse,
    private val notifyOperatorSaleDecisionCaseUse: NotifyOperatorSaleDecisionCaseUse,
    private val registerOperatorSaleRecordCaseUse: RegisterOperatorSaleRecordCaseUse,
    private val applyOperatorStockDecisionCaseUse: ApplyOperatorStockDecisionCaseUse
) : ViewModel() {

    companion object {
        private const val TAG = "OperatorSalesVM"

        /**
         * Resuelve el amount final según SALE_POLICY.
         */
        fun resolveConfirmedAmount(
            listAmount: Double,
            saleType: SaleType,
            discountAmount: Double?
        ): Double = when (saleType) {
            SaleType.GIFT -> 0.0
            SaleType.DISCOUNT -> {
                val effective = discountAmount
                    ?: error("DISCOUNT requiere importe efectivo")
                require(effective >= 0.0) { "El importe con descuento no puede ser negativo" }
                require(listAmount <= 0.0 || effective < listAmount) {
                    "El importe con descuento debe ser menor al precio de lista ($listAmount)"
                }
                effective
            }
            SaleType.NORMAL -> listAmount
        }
    }

    val recentSales = observeAllSalesCaseUse()
        .map { sales -> sales.sortedByDescending { it.date }.take(20) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(OperatorSalesUiState())
    val uiState: StateFlow<OperatorSalesUiState> = _uiState.asStateFlow()

    fun loadSaleByCode(rawPayload: String, onLoaded: () -> Unit = {}) {
        val saleId = extractSaleId(rawPayload)
        if (saleId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "No se pudo extraer un codigo de venta valido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = "Cargando datos de la reserva...",
                error = null,
                notice = null
            )
            runCatching {
                val remoteSale = saleNetRepository.getById(saleId)
                saleDao.insert(remoteSale)
                remoteSale.toDomain()
            }.onSuccess { sale ->
                _uiState.value = OperatorSalesUiState(
                    selectedSale = sale,
                    lastScannedPayload = rawPayload,
                    notice = "Reserva cargada correctamente."
                )
                onLoaded()
            }.onFailure { error ->
                _uiState.value = OperatorSalesUiState(
                    error = error.message ?: "No se encontro la reserva solicitada.",
                    lastScannedPayload = rawPayload
                )
            }
        }
    }

    fun selectSale(sale: Sale, onLoaded: () -> Unit = {}) {
        _uiState.value = _uiState.value.copy(selectedSale = sale, error = null, notice = null)
        onLoaded()
    }

    /**
     * Confirma la venta con [saleType] y, si es DISCOUNT, [discountAmount] efectivo.
     */
    fun confirmSelectedSale(
        saleType: SaleType = SaleType.NORMAL,
        discountAmount: Double? = null,
        onDone: () -> Unit = {}
    ) {
        decideSelectedSale(
            isSuccess = true,
            saleType = saleType,
            discountAmount = discountAmount,
            onDone = onDone
        )
    }

    fun rejectSelectedSale(onDone: () -> Unit = {}) {
        decideSelectedSale(
            isSuccess = false,
            saleType = null,
            discountAmount = null,
            onDone = onDone
        )
    }

    private fun decideSelectedSale(
        isSuccess: Boolean,
        saleType: SaleType?,
        discountAmount: Double?,
        onDone: () -> Unit
    ) {
        val selectedSale = _uiState.value.selectedSale ?: run {
            _uiState.value = _uiState.value.copy(error = "No hay una venta seleccionada.")
            return
        }

        viewModelScope.launch {
            val effectiveType = if (isSuccess) (saleType ?: SaleType.NORMAL) else null

            val resolvedAmount = if (isSuccess && effectiveType != null) {
                runCatching {
                    resolveConfirmedAmount(selectedSale.amount, effectiveType, discountAmount)
                }.getOrElse { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadingMessage = null,
                        error = error.message ?: "Importe invalido para el tipo de venta."
                    )
                    return@launch
                }
            } else {
                selectedSale.amount
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = if (isSuccess) {
                    "Confirmando venta en Appwrite..."
                } else {
                    "Rechazando venta en Appwrite..."
                },
                error = null,
                notice = null
            )

            Log.i(
                TAG,
                "event=operator_sale_decide saleId=${selectedSale.id} " +
                    "userId=${selectedSale.userId} decision=${if (isSuccess) "confirmed" else "rejected"} " +
                    "saleType=${effectiveType?.name ?: "n/a"} amount=$resolvedAmount " +
                    "currency=${selectedSale.currency.name}"
            )

            updateSaleVerificationFromRealtimeCaseUse(
                saleId = selectedSale.id,
                isSuccess = isSuccess,
                saleType = effectiveType,
                amountOverride = if (effectiveType == SaleType.DISCOUNT) discountAmount else null
            )
                .onSuccess {
                    val nextState = if (isSuccess) BuyState.VERIFIED else BuyState.DELETED
                    val resolvedType = if (isSuccess) effectiveType else selectedSale.saleType
                    val optimisticSale = selectedSale.copy(
                        verified = nextState,
                        saleType = resolvedType,
                        amount = resolvedAmount
                    )

                    Log.i(
                        TAG,
                        "event=operator_sale_remote_ok saleId=${selectedSale.id} " +
                            "nextState=$nextState saleType=${resolvedType?.name} amount=$resolvedAmount " +
                            "currency=${selectedSale.currency.name}"
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        loadingMessage = "Verificando el estado en Appwrite..."
                    )

                    val remoteVerification = runCatching {
                        val remoteSale = saleNetRepository.getById(selectedSale.id)
                        saleDao.insert(remoteSale)
                        remoteSale.toDomain()
                    }

                    val confirmedRemoteSale = remoteVerification.getOrElse { error ->
                        Log.e(
                            TAG,
                            "event=operator_sale_verify_failure saleId=${selectedSale.id} cause=${error.message}",
                            error
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loadingMessage = null,
                            selectedSale = optimisticSale,
                            error = error.message ?: "Appwrite no confirmo el cambio de estado."
                        )
                        onDone()
                        return@onSuccess
                    }

                    val saleForSideEffects = confirmedRemoteSale.copy(
                        saleType = confirmedRemoteSale.saleType ?: resolvedType,
                        amount = if (isSuccess) resolvedAmount else confirmedRemoteSale.amount
                    )

                    if (saleForSideEffects.verified != nextState) {
                        Log.e(
                            TAG,
                            "event=operator_sale_state_mismatch saleId=${saleForSideEffects.id} " +
                                "expected=$nextState actual=${saleForSideEffects.verified}"
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loadingMessage = null,
                            selectedSale = saleForSideEffects,
                            error = "Appwrite no confirmo el estado esperado para la venta ${selectedSale.id}."
                        )
                        onDone()
                        return@onSuccess
                    }

                    Log.i(
                        TAG,
                        "event=operator_sale_verified saleId=${saleForSideEffects.id} " +
                            "amount=${saleForSideEffects.amount} currency=${saleForSideEffects.currency.name}"
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        loadingMessage = if (isSuccess) {
                            "Actualizando inventario..."
                        } else {
                            "Liberando reservas de stock..."
                        }
                    )

                    val stockResult = runCatching {
                        applyOperatorStockDecisionCaseUse(saleForSideEffects, isSuccess)
                    }
                    val stockWarning = stockResult.exceptionOrNull()?.let { error ->
                        Log.e(
                            TAG,
                            "event=operator_stock_failure saleId=${saleForSideEffects.id} cause=${error.message}",
                            error
                        )
                        "Advertencia de stock: ${error.message ?: "no se pudo actualizar inventario."}"
                    }
                    if (stockResult.isSuccess) {
                        Log.i(
                            TAG,
                            "event=operator_stock_success saleId=${saleForSideEffects.id} confirmed=$isSuccess"
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        loadingMessage = "Notificando decision..."
                    )

                    val realtimeResult = runCatching {
                        notifyOperatorSaleDecisionCaseUse(saleForSideEffects, isSuccess)
                    }
                    val realtimeWarning = realtimeResult.exceptionOrNull()?.let { error ->
                        Log.e(
                            TAG,
                            "event=operator_pusher_failure saleId=${saleForSideEffects.id} cause=${error.message}",
                            error
                        )
                        "Aviso realtime: ${error.message ?: "no se pudo notificar por canal legado."}"
                    }
                    if (realtimeResult.isSuccess) {
                        Log.i(TAG, "event=operator_pusher_success saleId=${saleForSideEffects.id}")
                    }

                    val action = if (isSuccess) {
                        OperatorSaleRecordAction.CONFIRMED
                    } else {
                        OperatorSaleRecordAction.REJECTED
                    }

                    val recordResult = runCatching {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            loadingMessage = "Guardando el registro local de la operacion..."
                        )
                        registerOperatorSaleRecordCaseUse(saleForSideEffects, action)
                    }
                    recordResult.onFailure { error ->
                        Log.e(
                            TAG,
                            "event=operator_local_record_failure saleId=${saleForSideEffects.id} cause=${error.message}",
                            error
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loadingMessage = null,
                            selectedSale = saleForSideEffects,
                            notice = if (isSuccess) {
                                "Venta confirmada (${resolvedType?.name ?: "NORMAL"}) por ${"%.2f".format(resolvedAmount)} ${saleForSideEffects.currency.name}."
                            } else {
                                "Venta rechazada en servidor."
                            },
                            error = listOfNotNull(
                                stockWarning,
                                realtimeWarning,
                                error.message ?: "No se pudo registrar la venta en el dispositivo."
                            ).joinToString(" ")
                        )
                        onDone()
                        return@onSuccess
                    }
                    Log.i(TAG, "event=operator_local_record_saved saleId=${saleForSideEffects.id} action=$action")

                    val baseNotice = if (isSuccess) {
                        "Venta confirmada como ${resolvedType?.name ?: "NORMAL"} " +
                            "(${"%.2f".format(resolvedAmount)} ${saleForSideEffects.currency.name}), stock actualizado."
                    } else {
                        "Venta rechazada, hold liberado y registrada correctamente."
                    }

                    val extraWarnings = listOfNotNull(stockWarning, realtimeWarning).joinToString(" ").ifBlank { null }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadingMessage = null,
                        selectedSale = saleForSideEffects,
                        notice = if (extraWarnings == null) baseNotice else "$baseNotice $extraWarnings",
                        error = null
                    )
                    onDone()
                }
                .onFailure { error ->
                    Log.e(
                        TAG,
                        "event=operator_sale_update_failure saleId=${selectedSale.id} cause=${error.message}",
                        error
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadingMessage = null,
                        error = error.message ?: "No se pudo actualizar la venta en Appwrite."
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = OperatorSalesUiState()
    }

    private fun extractSaleId(rawPayload: String): String {
        val trimmed = rawPayload.trim()
        if (trimmed.isBlank()) return ""
        // Prefer explicit id query/path patterns if present
        val idFromQuery = Regex("[?&]id=([^&]+)").find(trimmed)?.groupValues?.getOrNull(1)
        if (!idFromQuery.isNullOrBlank()) return idFromQuery.trim()
        val lastSegment = trimmed.substringAfterLast('/').substringBefore('?').trim()
        return lastSegment.ifBlank { trimmed }
    }
}

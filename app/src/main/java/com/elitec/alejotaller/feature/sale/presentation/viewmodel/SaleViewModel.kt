package com.elitec.alejotaller.feature.sale.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.product.domain.caseUse.ApplySoftHoldCaseUse
import com.elitec.alejotaller.feature.product.domain.caseUse.CheckAProductExistenceCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.InitiatePaymentCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.GetSalesByIdCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.SyncSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateDeliveryTypeCaseUse
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryType
import com.elitec.shared.sale.feature.sale.domain.entity.PaymentChannel
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SaleViewModel(
        observeProductsCaseUse: ObserveAllSalesCaseUse,
        private val syncSalesCaseUse: SyncSalesCaseUse,
        private val getSaleByIdCaseUse: GetSalesByIdCaseUse,
        private val registerNewSaleCauseUse: RegisterNewSaleCauseUse,
        private val initiatePaymentCaseUse: InitiatePaymentCaseUse,
        private val updateDeliveryTypeCaseUse: UpdateDeliveryTypeCaseUse,
        private val checkAProductExistenceCaseUse: CheckAProductExistenceCaseUse,
        private val applySoftHoldCaseUse: ApplySoftHoldCaseUse
) : ViewModel() {

    val salesFlow =
            observeProductsCaseUse()
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun sync(userId: String) {
        viewModelScope.launch { syncSalesCaseUse(userId) }
    }

    fun getSaleById(id: String, onSaleCharge: (Sale?) -> Unit) {
        viewModelScope.launch { getSaleByIdCaseUse(id).onSuccess { onSaleCharge(it) } }
    }

    fun newSale(sale: Sale, onSaleRegistered: (String) -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            checkAProductExistenceCaseUse(sale)
                    .onFailure { error ->
                        onFail(error.message ?: "Sin disponibilidad de stock")
                        return@launch
                    }

            registerNewSaleCauseUse(sale)
                    .onSuccess { transferId ->
                        val saleWithId = sale.copy(id = transferId)
                        applySoftHoldCaseUse(saleWithId)
                                .onFailure { /* best-effort: pedido ya persistido */ }
                        onSaleRegistered(transferId)
                    }
                    .onFailure { error -> onFail(error.message ?: "") }
        }
    }

    /**
     * Soft-check de stock, registra la venta y genera la URL de pago.
     */
    fun initiatePayment(
        sale: Sale,
        paymentChannel: PaymentChannel,
        onReadyToPay: (saleId: String, checkoutUrl: String) -> Unit,
        onFail: (String) -> Unit
    ) {
        viewModelScope.launch {
            checkAProductExistenceCaseUse(sale)
                    .onFailure { error ->
                        onFail(error.message ?: "Sin disponibilidad de stock")
                        return@launch
                    }

            initiatePaymentCaseUse(sale, paymentChannel)
                    .onSuccess { result ->
                        val saleWithId = sale.copy(id = result.saleId)
                        applySoftHoldCaseUse(saleWithId)
                                .onFailure { /* best-effort */ }
                        onReadyToPay(result.saleId, result.checkoutUrl)
                    }
                    .onFailure { error ->
                        onFail(error.message ?: "Error desconocido al procesar el pedido")
                    }
        }
    }

    fun updateDeliveryType(
            saleId: String,
            deliveryType: DeliveryType,
            onSuccess: () -> Unit = {},
            onFail: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            updateDeliveryTypeCaseUse(saleId, deliveryType).onSuccess { onSuccess() }.onFailure {
                    error ->
                onFail(error.message ?: "No se pudo actualizar la preferencia de entrega")
            }
        }
    }
}

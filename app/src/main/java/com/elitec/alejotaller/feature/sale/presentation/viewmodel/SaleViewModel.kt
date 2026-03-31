package com.elitec.alejotaller.feature.sale.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.sale.domain.caseUse.InitiatePaymentCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.SyncSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.GetSalesByIdCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.RegisterNewSaleCauseUse
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
        private val updateDeliveryTypeCaseUse: UpdateDeliveryTypeCaseUse
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
            registerNewSaleCauseUse(sale)
                    .onSuccess { transferId -> onSaleRegistered(transferId) }
                    .onFailure { error -> onFail(error.message ?: "") }
        }
    }

    /**
     * Registra la venta (Telegram + Room) y genera la URL de pago.
     *
     * @param onReadyToPay Recibe (saleId, checkoutUrl) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â la UI abre la URL en Chrome Tabs.
     * ```
     *                       Si checkoutUrl es null, la venta estÃƒÆ’Ã‚Â¡ registrada pero sin pago online.
     * @param onFail
     * ```
     * Error crÃƒÆ’Ã‚Â­tico (no se pudo ni guardar la venta).
     */
    fun initiatePayment(
        sale: Sale,
        paymentChannel: PaymentChannel,
        onReadyToPay: (saleId: String, checkoutUrl: String) -> Unit,
        onFail: (String) -> Unit
    ) {
        viewModelScope.launch {
            initiatePaymentCaseUse(sale, paymentChannel)
                    .onSuccess { result ->
                        onReadyToPay(result.saleId, result.checkoutUrl)
                    }
                    .onFailure { error ->
                        onFail(error.message ?: "Error desconocido al procesar el pedido")
                    }
        }
    }

    /**
     * Actualiza la preferencia de entrega (recoger en tienda / domicilio) de una venta en estado
     * VERIFIED.
     *
     * Persiste el cambio localmente y lo sincroniza con Appwrite. La UI muestra el mensaje de
     * confirmaciÃƒÆ’Ã‚Â³n a travÃƒÆ’Ã‚Â©s de [onSuccess]/[onFail].
     */
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

package com.elitec.alejotallerscan.feature.confirmation.domain.repository

import com.elitec.shared.sale.feature.sale.domain.entity.Sale

interface OperatorSaleRealtimeNotifier {
    suspend fun notifySaleDecision(sale: Sale, isSuccess: Boolean)
}

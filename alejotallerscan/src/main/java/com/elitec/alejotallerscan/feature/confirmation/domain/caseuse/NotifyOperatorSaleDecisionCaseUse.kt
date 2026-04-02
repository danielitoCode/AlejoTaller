package com.elitec.alejotallerscan.feature.confirmation.domain.caseuse

import com.elitec.alejotallerscan.feature.confirmation.domain.repository.OperatorSaleRealtimeNotifier
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

class NotifyOperatorSaleDecisionCaseUse(
    private val notifier: OperatorSaleRealtimeNotifier
) {
    suspend operator fun invoke(sale: Sale, isSuccess: Boolean) {
        notifier.notifySaleDecision(sale, isSuccess)
    }
}

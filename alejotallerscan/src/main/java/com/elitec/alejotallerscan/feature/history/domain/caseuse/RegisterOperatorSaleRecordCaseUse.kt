package com.elitec.alejotallerscan.feature.history.domain.caseuse

import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecordAction
import com.elitec.alejotallerscan.feature.history.domain.repository.OperatorSaleRecordRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

class RegisterOperatorSaleRecordCaseUse(
    private val repository: OperatorSaleRecordRepository
) {
    suspend operator fun invoke(sale: Sale, action: OperatorSaleRecordAction) {
        repository.register(sale, action)
    }
}

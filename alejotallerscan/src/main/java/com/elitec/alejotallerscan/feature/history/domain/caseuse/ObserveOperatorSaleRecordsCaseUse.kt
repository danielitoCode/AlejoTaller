package com.elitec.alejotallerscan.feature.history.domain.caseuse

import com.elitec.alejotallerscan.feature.history.domain.repository.OperatorSaleRecordRepository

class ObserveOperatorSaleRecordsCaseUse(
    private val repository: OperatorSaleRecordRepository
) {
    operator fun invoke() = repository.observeAll()
}

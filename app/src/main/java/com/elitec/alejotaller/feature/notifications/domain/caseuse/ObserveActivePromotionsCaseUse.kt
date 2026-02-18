package com.elitec.alejotaller.feature.notifications.domain.caseuse

import com.elitec.alejotaller.feature.notifications.domain.repository.PromotionRepository

class ObserveActivePromotionsCaseUse(
    private val repository: PromotionRepository
) {
    operator fun invoke(nowEpochMillis: Long = System.currentTimeMillis()) =
        repository.observeActivePromotions(nowEpochMillis)
}
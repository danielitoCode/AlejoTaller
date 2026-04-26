package com.elitec.alejotaller.feature.notifications.domain.caseuse

import com.elitec.alejotaller.feature.notifications.domain.repository.PromotionRepository
import com.elitec.shared.core.feature.notifications.domain.entity.Promotion

class SavePromotionCaseUse(
    private val repository: PromotionRepository
) {
    suspend operator fun invoke(promotion: Promotion) =
        repository.savePromotion(promotion)
}
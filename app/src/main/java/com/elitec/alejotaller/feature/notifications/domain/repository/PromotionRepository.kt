package com.elitec.alejotaller.feature.notifications.domain.repository

import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import kotlinx.coroutines.flow.Flow

interface PromotionRepository {
    fun observeActivePromotions(nowEpochMillis: Long): Flow<List<Promotion>>
    suspend fun savePromotion(promotion: Promotion)
}
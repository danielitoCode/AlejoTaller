package com.elitec.alejotaller.feature.notifications.data.repository

import com.elitec.alejotaller.feature.notifications.data.dao.PromotionDao
import com.elitec.alejotaller.feature.notifications.data.mappers.toDomain
import com.elitec.alejotaller.feature.notifications.data.mappers.toDto
import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.notifications.domain.repository.PromotionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PromotionOfflineFirstRepository(
    private val dao: PromotionDao
) : PromotionRepository {

    override fun observeActivePromotions(nowEpochMillis: Long): Flow<List<Promotion>> =
        dao.observeActive(nowEpochMillis).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun savePromotion(promotion: Promotion) {
        dao.clearExpired(System.currentTimeMillis())
        dao.upsert(promotion.toDto())
    }
}
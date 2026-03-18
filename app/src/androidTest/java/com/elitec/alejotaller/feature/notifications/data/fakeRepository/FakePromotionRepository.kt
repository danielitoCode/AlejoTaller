package com.elitec.alejotaller.feature.notifications.data.fakeRepository

import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.notifications.domain.repository.PromotionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class FakePromotionRepository: PromotionRepository {
    private val fakePromotionListFlow = MutableStateFlow(listOf<Promotion>())
    private val fakePromotionList = listOf(
        Promotion(
            id = "promotionTestId 1",
            title = "Promotion test 1",
            message = "Message of promotion test 1",
            imageUrl = "promotion photo url test 1",
            oldPrice = 11.0,
            currentPrice = 10.0,
            validFromEpochMillis = Clock.System.now().toEpochMilliseconds(),
            validUntilEpochMillis = Clock.System.now().toEpochMilliseconds()
        ),
        Promotion(
            id = "promotionTestId 2",
            title = "Promotion test 2",
            message = "Message of promotion test 2",
            imageUrl = "promotion photo url test 2",
            oldPrice = 12.0,
            currentPrice = 10.0,
            validFromEpochMillis = Clock.System.now().toEpochMilliseconds(),
            validUntilEpochMillis = Clock.System.now().toEpochMilliseconds()
        ),
        Promotion(
            id = "promotionTestId 3",
            title = "Promotion test 3",
            message = "Message of promotion test 3",
            imageUrl = "promotion photo url test 3",
            oldPrice = 13.0,
            currentPrice = 10.0,
            validFromEpochMillis = Clock.System.now().toEpochMilliseconds(),
            validUntilEpochMillis = Clock.System.now().toEpochMilliseconds()
        )
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                fakePromotionList.forEachIndexed { index, promotion ->
                    fakePromotionListFlow.update { list ->
                        val tempPromotionList = list.toMutableList()
                        tempPromotionList.add(promotion)
                        tempPromotionList
                    }
                    delay(400L * index)
                }
            }
        }
    }

    override fun observeActivePromotions(nowEpochMillis: Long): Flow<List<Promotion>> = fakePromotionListFlow.asStateFlow()

    override suspend fun savePromotion(promotion: Promotion) {}
}
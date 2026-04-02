package com.elitec.shared.core.feature.notifications.domain.entity

import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class PromotionTest {

    @Test
    fun `isActive devuelve true dentro del rango`() {
        val promotion = Promotion(
            id = "promo-1",
            title = "Promo",
            message = "Activa",
            imageUrl = null,
            validFromEpochMillis = 100L,
            validUntilEpochMillis = 200L
        )

        assertTrue(promotion.isActive(100L))
        assertTrue(promotion.isActive(150L))
        assertTrue(promotion.isActive(200L))
    }

    @Test
    fun `isActive devuelve false fuera del rango`() {
        val promotion = Promotion(
            id = "promo-1",
            title = "Promo",
            message = "Activa",
            imageUrl = null,
            validFromEpochMillis = 100L,
            validUntilEpochMillis = 200L
        )

        assertFalse(promotion.isActive(99L))
        assertFalse(promotion.isActive(201L))
    }
}

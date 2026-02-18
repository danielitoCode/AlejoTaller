package com.elitec.alejotaller.feature.notifications.domain.entity

data class PromotionEvent(
    val id: String,
    val title: String,
    val message: String,
    val imageUrl: String? = null
)
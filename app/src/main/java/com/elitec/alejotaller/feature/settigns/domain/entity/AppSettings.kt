package com.elitec.alejotaller.feature.settigns.domain.entity

data class AppSettings(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = false
)
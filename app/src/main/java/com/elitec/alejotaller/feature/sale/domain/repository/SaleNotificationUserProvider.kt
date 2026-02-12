package com.elitec.alejotaller.feature.sale.domain.repository

import com.elitec.alejotaller.feature.sale.domain.entity.SaleNotifierUser

interface SaleNotificationUserProvider {
    suspend fun getCurrentUser(): Result<SaleNotifierUser>
}
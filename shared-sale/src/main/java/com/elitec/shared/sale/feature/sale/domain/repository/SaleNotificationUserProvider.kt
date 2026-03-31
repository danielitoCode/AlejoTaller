package com.elitec.shared.sale.feature.sale.domain.repository

import com.elitec.shared.sale.feature.sale.domain.entity.SaleNotifierUser

interface SaleNotificationUserProvider {
    suspend fun getCurrentUser(): Result<SaleNotifierUser>
}

package com.elitec.alejotallerscan.feature.sync.domain.repository

interface OperatorSyncNotificationService {
    fun showSaleVerified(saleId: String, customerName: String?)
}

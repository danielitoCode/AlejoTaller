package com.elitec.alejotallerscan.feature.sync.domain.caseuse

import android.util.Log
import com.elitec.alejotallerscan.feature.sync.domain.repository.OperatorSyncNotificationService
import com.elitec.shared.data.feature.sale.data.dao.SaleDao
import com.elitec.shared.data.feature.sale.data.mapper.toDomain
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncPendingOperatorSalesCaseUse(
    private val saleDao: SaleDao,
    private val saleNetRepository: SaleNetRepository,
    private val notificationService: OperatorSyncNotificationService
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val localPendingSales = saleDao.getAll()
            .filter { it.verified == BuyState.UNVERIFIED.name }

        Log.i(TAG, "event=operator_pending_sync_start pendingCount=${localPendingSales.size}")

        localPendingSales.forEach { localSale ->
            runCatching {
                saleNetRepository.getById(localSale.id)
            }.onSuccess { remoteSale ->
                if (remoteSale.verified != localSale.verified) {
                    saleDao.insert(remoteSale)
                    Log.i(
                        TAG,
                        "event=operator_pending_sync_updated saleId=${remoteSale.id} " +
                            "from=${localSale.verified} to=${remoteSale.verified}"
                    )
                    if (remoteSale.verified == BuyState.VERIFIED.name) {
                        notificationService.showSaleVerified(
                            saleId = remoteSale.id,
                            customerName = remoteSale.customerName
                        )
                        Log.i(TAG, "event=operator_pending_sync_notified saleId=${remoteSale.id}")
                    }
                }
            }.onFailure { error ->
                Log.w(
                    TAG,
                    "event=operator_pending_sync_failed saleId=${localSale.id} cause=${error.message}",
                    error
                )
            }
        }
    }

    private companion object {
        const val TAG = "OperatorPendingSync"
    }
}

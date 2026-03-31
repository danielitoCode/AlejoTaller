package com.elitec.shared.data.feature.sale.data.repository

import android.util.Log
import com.elitec.shared.data.feature.sale.data.dao.SaleDao
import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.data.feature.sale.data.mapper.toDomain
import com.elitec.shared.data.feature.sale.data.mapper.toDto
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val MAX_SYNC_RETRIES = 3
private const val TAG = "SaleOfflineRepository"

class SaleOfflineFirstRepository(
    private val net: SaleNetRepository,
    private val bd: SaleDao
) : SaleRepository {
    override fun observeAll(): Flow<List<Sale>> =
        bd.getAllFlow().map { saleDtoList ->
            saleDtoList.map { saleDto -> saleDto.toDomain() }
        }

    override suspend fun getById(itemId: String): Sale = bd.getById(itemId).toDomain()

    override suspend fun save(item: Sale) {
        val saleDto = item.toDto()
        Log.i(TAG, "event=sale_save_local_start saleId=${saleDto.id} userId=${saleDto.userId}")
        bd.insert(saleDto)
        Log.i(TAG, "event=sale_save_local_success saleId=${saleDto.id}")
        runCatching {
            Log.i(TAG, "event=sale_save_remote_push_start saleId=${saleDto.id}")
            pushWithRetry(net, saleDto)
            Log.i(TAG, "event=sale_save_remote_push_success saleId=${saleDto.id}")
        }.onFailure { error ->
            Log.w(TAG, "event=sale_save_remote_push_failed saleId=${saleDto.id} cause=${error.message}", error)
        }
    }

    override suspend fun sync(userId: String): Result<Unit> = runCatching {
        val localAllSales = bd.getAll()
        val localTargetUserSales = localAllSales.filter { sale -> sale.userId == userId }
        val localOtherUserSales = localAllSales.filter { sale -> sale.userId != userId }

        val remoteBeforePush = net.getAll(userId)
        val pendingQueue = resolvePendingLocals(
            localSales = localTargetUserSales,
            remoteSales = remoteBeforePush
        )

        val failedToPush = pendingQueue.filter { pendingSale ->
            runCatching { pushWithRetry(net, pendingSale) }.isFailure
        }

        val remoteAfterPush = net.getAll(userId)
        val mergedTargetUserSales = mergeSyncResult(
            remoteSales = remoteAfterPush,
            failedPendingSales = failedToPush
        )

        bd.replaceAll(localOtherUserSales + mergedTargetUserSales)
    }
}
internal fun resolvePendingLocals(
    localSales: List<SaleDto>,
    remoteSales: List<SaleDto>
): List<SaleDto> {
    val remoteIds = remoteSales.map { sale -> sale.id }.toSet()
    return localSales.filter { sale -> sale.id !in remoteIds }
}

internal fun mergeSyncResult(
    remoteSales: List<SaleDto>,
    failedPendingSales: List<SaleDto>
): List<SaleDto> {
    val mergedById = remoteSales.associateBy { sale -> sale.id }.toMutableMap()
    failedPendingSales.forEach { failedSale ->
        if (failedSale.id !in mergedById) {
            mergedById[failedSale.id] = failedSale
        }
    }
    return mergedById.values.toList()
}

private suspend fun pushWithRetry(
    net: SaleNetRepository,
    sale: SaleDto,
    maxRetries: Int = MAX_SYNC_RETRIES
) {
    var lastError: Throwable? = null
    repeat(maxRetries) { attempt ->
        runCatching { net.upsert(sale) }
            .onSuccess {
                Log.i(TAG, "event=sale_push_retry_success saleId=${sale.id} attempt=${attempt + 1}")
                return
            }
            .onFailure { error ->
                lastError = error
                Log.w(TAG, "event=sale_push_retry_failed saleId=${sale.id} attempt=${attempt + 1} cause=${error.message}")
            }
    }

    throw (lastError ?: IllegalStateException("No se pudo sincronizar la venta ${sale.id}"))
}

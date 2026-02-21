package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.feature.product.data.mapper.toDomain
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.sale.data.dao.SaleDao
import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.data.mapper.toDomain
import com.elitec.alejotaller.feature.sale.data.mapper.toDto
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val MAX_SYNC_RETRIES = 3

class SaleOfflineFirstRepository(
    private val net: SaleNetRepositoryImpl,
    private val bd: SaleDao
) : SaleRepository {
    override fun observeAll(): Flow<List<Sale>> =
        bd.getAllFlow().map { saleDtoList ->
            saleDtoList.map { saleDto -> saleDto.toDomain() }
        }

    override suspend fun getById(itemId: String): Sale = bd.getById(itemId).toDomain()

    override suspend fun save(item: Sale) {
        val saleDto = item.toDto()
        bd.insert(saleDto)
        runCatching { pushWithRetry(net, saleDto) }
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
    net: SaleNetRepositoryImpl,
    sale: SaleDto,
    maxRetries: Int = MAX_SYNC_RETRIES
) {
    var lastError: Throwable? = null
    repeat(maxRetries) {
        runCatching { net.upsert(sale) }
            .onSuccess { return }
            .onFailure { error -> lastError = error }
    }

    throw (lastError ?: IllegalStateException("No se pudo sincronizar la venta ${sale.id}"))
}
package com.elitec.shared.data.feature.sale.data.repository

import android.util.Log
import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.data.feature.sale.data.mapper.toSaleDto
import com.elitec.shared.data.infraestructure.core.data.config.SaleRemoteConfig
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.serialization.json.Json

class SaleNetRepositoryImpl(
    private val netDB: TablesDB,
    private val config: SaleRemoteConfig
): SaleNetRepository {
    override suspend fun getAll(userId: String): List<SaleDto> {
        Log.i(TAG, "event=sale_net_get_all_start userId=$userId collection=${config.saleCollectionId}")
        val response = netDB.listRows(
            databaseId = config.databaseId,
            tableId = config.saleCollectionId,
            queries = listOf(Query.equal("user_id", userId))
        )
        Log.i(TAG, "event=sale_net_get_all_success userId=$userId count=${response.rows.size}")
        return response.rows.map { row -> row.toSaleDto() }
    }

    override suspend fun getById(itemId: String): SaleDto {
        Log.i(TAG, "event=sale_net_get_by_id_start saleId=$itemId collection=${config.saleCollectionId}")
        val response = netDB.getRow(
            databaseId = config.databaseId,
            tableId = config.saleCollectionId,
            rowId = itemId
        )
        Log.i(TAG, "event=sale_net_get_by_id_success saleId=$itemId")
        return response.toSaleDto()
    }

    override suspend fun search(field: String, query: String, limit: Int): List<SaleDto> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return emptyList()

        val response = netDB.listRows(
            databaseId = config.databaseId,
            tableId = config.saleCollectionId,
            queries = listOf(Query.limit(limit))
        )

        return response.rows
            .map { it.toSaleDto() }
            .filter { sale ->
                when (field.uppercase()) {
                    "SALE_ID" -> sale.id.contains(normalizedQuery, ignoreCase = true)
                    "USER_ID" -> sale.userId.contains(normalizedQuery, ignoreCase = true)
                    "CUSTOMER_NAME" -> sale.customerName?.contains(normalizedQuery, ignoreCase = true) == true
                    "DATE" -> sale.date.toString().contains(normalizedQuery, ignoreCase = true)
                    else -> false
                }
            }
    }

    override suspend fun save(item: SaleDto) {
        val resolvedId = item.id.ifBlank { ID.unique() }
        Log.i(TAG, "event=sale_net_save_start saleId=$resolvedId userId=${item.userId} verified=${item.verified}")
        netDB.createRow(
            databaseId = config.databaseId,
            tableId = config.saleCollectionId,
            rowId = resolvedId,
            data = item.toAppwriteData()
        )
        Log.i(TAG, "event=sale_net_save_success saleId=$resolvedId")
    }

    override suspend fun upsert(item: SaleDto) {
        if (item.id.isBlank()) return
        Log.i(TAG, "event=sale_net_upsert_start saleId=${item.id} userId=${item.userId} verified=${item.verified}")
        netDB.updateRow(
            databaseId = config.databaseId,
            tableId = config.saleCollectionId,
            rowId = item.id,
            data = item.toAppwriteData()
        )
        Log.i(TAG, "event=sale_net_upsert_updated saleId=${item.id}")
    }

    companion object {
        private const val TAG = "SaleNetRepository"
    }
}

internal fun SaleDto.toAppwriteData(): Map<String, Any?> = mapOf(
    "date" to date.toString(),
    "amount" to amount,
    "buy_state" to verified,
    "products" to Json.encodeToString(
        products.map { product ->
            AppwriteSaleProductPayload(
                productId = product.productId,
                quantity = product.quantity,
                price = null
            )
        }
    ),
    "user_id" to userId,
    "customer_name" to customerName,
    "delivery_type" to deliveryType,
    "delivery_address" to deliveryAddress,
    "sale_type" to saleType,
    "stock_hold_applied" to stockHoldApplied
).filterValues { value -> value != null }



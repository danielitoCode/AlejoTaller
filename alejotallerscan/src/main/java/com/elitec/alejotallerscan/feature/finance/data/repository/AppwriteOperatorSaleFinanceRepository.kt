package com.elitec.alejotallerscan.feature.finance.data.repository

import android.util.Log
import com.elitec.alejotallerscan.BuildConfig
import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceRecord
import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceWrite
import com.elitec.alejotallerscan.feature.finance.domain.repository.OperatorSaleFinanceRepository
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases

class AppwriteOperatorSaleFinanceRepository(
    private val databases: Databases
) : OperatorSaleFinanceRepository {

    private val collectionId: String
        get() = BuildConfig.SALE_FINANCE_EVENT_TABLE_ID.ifBlank { "sale_finance_event" }

    override suspend fun getBySaleId(saleId: String): SaleFinanceRecord? {
        val sid = saleId.trim()
        if (sid.isEmpty()) return null
        val response = databases.listDocuments(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = collectionId,
            queries = listOf(
                Query.equal("sale_id", sid),
                Query.limit(1)
            )
        )
        val doc = response.documents.firstOrNull() ?: return null
        val data = doc.data
        return SaleFinanceRecord(
            id = doc.id,
            saleId = (data["sale_id"] as? String).orEmpty(),
            revenue = (data["revenue"] as? Number)?.toDouble() ?: 0.0,
            cogs = (data["cogs"] as? Number)?.toDouble() ?: 0.0,
            margin = (data["margin"] as? Number)?.toDouble() ?: 0.0
        )
    }

    override suspend fun createIdempotent(event: SaleFinanceWrite): SaleFinanceRecord {
        val existing = getBySaleId(event.saleId)
        if (existing != null) {
            Log.i(TAG, "event=operator_finance_idempotent saleId=${event.saleId} id=${existing.id}")
            return existing
        }

        val id = ID.unique()
        val data = mutableMapOf<String, Any>(
            "sale_id" to event.saleId,
            "revenue" to event.revenue,
            "cogs" to event.cogs,
            "margin" to event.margin,
            "user_id" to event.userId,
            "at" to event.atIso
        )
        event.currency?.takeIf { it.isNotBlank() }?.let { data["currency"] = it }

        val doc = databases.createDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = collectionId,
            documentId = id,
            data = data
        )
        Log.i(
            TAG,
            "event=operator_finance_created id=${doc.id} saleId=${event.saleId} " +
                "revenue=${event.revenue} cogs=${event.cogs} margin=${event.margin}"
        )
        return SaleFinanceRecord(
            id = doc.id,
            saleId = event.saleId,
            revenue = event.revenue,
            cogs = event.cogs,
            margin = event.margin
        )
    }

    companion object {
        private const val TAG = "OperatorFinanceRepo"
    }
}

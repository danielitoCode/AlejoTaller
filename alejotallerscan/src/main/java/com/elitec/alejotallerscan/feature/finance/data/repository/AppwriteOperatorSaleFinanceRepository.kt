package com.elitec.alejotallerscan.feature.finance.data.repository

import android.util.Log
import com.elitec.alejotallerscan.BuildConfig
import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceLineWrite
import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceRecord
import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceWrite
import com.elitec.alejotallerscan.feature.finance.domain.repository.OperatorSaleFinanceRepository
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases
import org.json.JSONArray
import org.json.JSONObject

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
            margin = (data["margin"] as? Number)?.toDouble() ?: 0.0,
            lines = parseLinesJson(data["lines_json"] as? String)
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
        if (event.lines.isNotEmpty()) {
            data["lines_json"] = serializeLines(event.lines)
        }

        val doc = databases.createDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = collectionId,
            documentId = id,
            data = data
        )
        Log.i(
            TAG,
            "event=operator_finance_created id=${doc.id} saleId=${event.saleId} " +
                "revenue=${event.revenue} cogs=${event.cogs} margin=${event.margin} lines=${event.lines.size}"
        )
        return SaleFinanceRecord(
            id = doc.id,
            saleId = event.saleId,
            revenue = event.revenue,
            cogs = event.cogs,
            margin = event.margin,
            lines = event.lines
        )
    }

    private fun serializeLines(lines: List<SaleFinanceLineWrite>): String {
        val arr = JSONArray()
        for (l in lines) {
            arr.put(
                JSONObject()
                    .put("productId", l.productId)
                    .put("quantity", l.quantity)
                    .put("unitPrice", l.unitPrice)
                    .put("unitCostSnapshot", l.unitCostSnapshot)
                    .put("lineRevenue", l.lineRevenue)
                    .put("lineCogs", l.lineCogs)
                    .put("lineMargin", l.lineMargin)
            )
        }
        return arr.toString()
    }

    private fun parseLinesJson(raw: String?): List<SaleFinanceLineWrite> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    val productId = o.optString("productId").ifBlank {
                        o.optString("product_id")
                    }
                    if (productId.isBlank()) continue
                    val quantity = o.optInt("quantity", 0)
                    if (quantity <= 0) continue
                    val unitPrice = o.optDouble("unitPrice", o.optDouble("unit_price", 0.0))
                    val unitCostSnapshot = o.optDouble(
                        "unitCostSnapshot",
                        o.optDouble("unit_cost_snapshot", 0.0)
                    )
                    val lineRevenue = o.optDouble("lineRevenue", unitPrice * quantity)
                    val lineCogs = o.optDouble("lineCogs", unitCostSnapshot * quantity)
                    val lineMargin = o.optDouble("lineMargin", lineRevenue - lineCogs)
                    add(
                        SaleFinanceLineWrite(
                            productId = productId,
                            quantity = quantity,
                            unitPrice = unitPrice,
                            unitCostSnapshot = unitCostSnapshot,
                            lineRevenue = lineRevenue,
                            lineCogs = lineCogs,
                            lineMargin = lineMargin
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val TAG = "OperatorFinanceRepo"
    }
}

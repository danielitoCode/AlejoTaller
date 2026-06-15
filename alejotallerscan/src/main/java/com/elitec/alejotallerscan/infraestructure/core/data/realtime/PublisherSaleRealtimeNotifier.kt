package com.elitec.alejotallerscan.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.alejotallerscan.feature.confirmation.domain.repository.OperatorSaleRealtimeNotifier
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class PublisherSaleRealtimeNotifier(
    private val client: OkHttpClient,
    private val config: OperatorPublisherConfig
) : OperatorSaleRealtimeNotifier {

    private val json = Json { encodeDefaults = true }

    override suspend fun notifySaleDecision(sale: Sale, isSuccess: Boolean) {
        withContext(Dispatchers.IO) {
            val payload = PublisherSaleDecisionRequest(
                saleId = sale.id,
                userId = sale.userId,
                decision = if (isSuccess) "confirmed" else "rejected",
                amount = sale.amount,
                productCount = sale.products.sumOf { it.quantity },
                cause = if (isSuccess) null else "rejected_by_operator"
            )
            val requestBody = json.encodeToString(payload)
            val endpoint = "${config.baseUrl.trimEnd('/')}/sale-verification/publish"

            Log.i(
                TAG,
                "event=operator_publisher_prepare saleId=${sale.id} userId=${sale.userId} " +
                    "endpoint=$endpoint payload=$requestBody"
            )

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                Log.i(
                    TAG,
                    "event=operator_publisher_response saleId=${sale.id} code=${response.code} " +
                        "successful=${response.isSuccessful} body=$responseBody"
                )
                if (!response.isSuccessful) {
                    error(
                        "No se pudo publicar el evento realtime via publisher. " +
                            "code=${response.code} body=$responseBody"
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "OperatorPublisher"
    }
}

data class OperatorPublisherConfig(
    val baseUrl: String,
    val apiKey: String
)

@Serializable
private data class PublisherSaleDecisionRequest(
    val saleId: String,
    val userId: String,
    val decision: String,
    val amount: Double,
    val productCount: Int,
    val cause: String? = null
)

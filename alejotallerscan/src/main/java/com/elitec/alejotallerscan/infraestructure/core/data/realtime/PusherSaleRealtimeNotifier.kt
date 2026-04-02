package com.elitec.alejotallerscan.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.alejotallerscan.feature.confirmation.domain.repository.OperatorSaleRealtimeNotifier
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PusherSaleRealtimeNotifier(
    private val client: OkHttpClient,
    private val config: OperatorPusherConfig
) : OperatorSaleRealtimeNotifier {

    private val json = Json { encodeDefaults = true }

    override suspend fun notifySaleDecision(sale: Sale, isSuccess: Boolean) {
        val eventName = if (isSuccess) "sale:confirmed" else "sale:rejected"
        val channel = "${config.saleChannelPrefix}-${sale.userId}"
        val decision = if (isSuccess) "confirmed" else "rejected"
        val payload = SaleRealtimePushPayload(
            saleId = sale.id,
            userId = sale.userId,
            decision = decision,
            timestamp = System.currentTimeMillis().toString(),
            amount = sale.amount,
            productCount = sale.products.sumOf { it.quantity },
            type = eventName,
            cause = if (isSuccess) null else "rejected_by_operator"
        )
        val requestBody = json.encodeToString(
            PusherEventRequest(
                name = eventName,
                channels = listOf(channel),
                data = json.encodeToString(payload)
            )
        )

        val timestamp = (System.currentTimeMillis() / 1000L).toString()
        val bodyMd5 = requestBody.md5()
        val path = "/apps/${config.appId}/events"
        val queryString = listOf(
            "auth_key=${config.apiKey}",
            "auth_timestamp=$timestamp",
            "auth_version=1.0",
            "body_md5=$bodyMd5"
        ).joinToString("&")
        val authSignature = "$path\n$queryString".hmacSha256(config.apiSecret)
        val url = "https://api-${config.cluster}.pusher.com$path?$queryString&auth_signature=$authSignature"

        Log.i(
            TAG,
            "event=operator_pusher_prepare saleId=${sale.id} userId=${sale.userId} " +
                "channel=$channel eventName=$eventName payload=$requestBody"
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            Log.i(
                TAG,
                "event=operator_pusher_response saleId=${sale.id} code=${response.code} " +
                    "successful=${response.isSuccessful} body=$responseBody"
            )
            if (!response.isSuccessful) {
                error(
                    "No se pudo publicar el evento realtime de venta. " +
                        "code=${response.code} body=$responseBody"
                )
            }
        }
    }

    companion object {
        private const val TAG = "OperatorPusher"
    }
}

data class OperatorPusherConfig(
    val appId: String,
    val apiKey: String,
    val apiSecret: String,
    val cluster: String,
    val saleChannelPrefix: String
)

@Serializable
private data class PusherEventRequest(
    val name: String,
    val channels: List<String>,
    val data: String
)

@Serializable
private data class SaleRealtimePushPayload(
    val saleId: String,
    val userId: String,
    val decision: String,
    val timestamp: String,
    val amount: Double,
    val productCount: Int,
    val type: String,
    val cause: String? = null
)

private fun String.md5(): String =
    MessageDigest.getInstance("MD5")
        .digest(toByteArray())
        .joinToString("") { "%02x".format(it) }

private fun String.hmacSha256(secret: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
    return mac.doFinal(toByteArray()).joinToString("") { "%02x".format(it) }
}

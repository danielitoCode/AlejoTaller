package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import org.json.JSONObject
import java.util.Locale

class SolucionesCubaPaymentGateway(
    private val httpClient: HttpClient
) {
    suspend fun createPaymentLink(
        amount: Double,
        reference: String,
        description: String
    ): Result<String> = runCatching {
        val endpoint = BuildConfig.SOLUCIONES_CUBA_PAY_API_URL
        require(endpoint.isNotBlank()) { "SOLUCIONES_CUBA_PAY_API_URL no está configurada" }

        val response = httpClient.submitForm(
            url = endpoint,
            formParameters = Parameters.build {
                append("api_key", BuildConfig.SOLUCIONES_CUBA_API_KEY)
                append("merchant_id", BuildConfig.SOLUCIONES_CUBA_MERCHANT_ID)
                append("amount", String.format(Locale.US, "%.2f", amount))
                append("currency", "CUP")
                append("reference", reference)
                append("description", description)
                append("success_url", "")// BuildConfig.SOLUCIONES_CUBA_SUCCESS_URL)
                append("cancel_url", "") // BuildConfig.SOLUCIONES_CUBA_CANCEL_URL)
                append("callback_url", "") //BuildConfig.SOLUCIONES_CUBA_CALLBACK_URL)
            }
        )

        val payload = response.bodyAsText().trim()
        extractCheckoutUrl(payload)
            ?: throw IllegalStateException("No se pudo obtener URL de pago desde la API")
    }

    private fun extractCheckoutUrl(rawPayload: String): String? {
        val direct = rawPayload.takeIf { it.startsWith("http://") || it.startsWith("https://") }
        if (direct != null) return direct

        return runCatching {
            val obj = JSONObject(rawPayload)
            listOf("checkout_url", "payment_url", "redirect_url", "url", "link")
                .firstNotNullOfOrNull { key ->
                    obj.optString(key).takeIf { it.startsWith("http://") || it.startsWith("https://") }
                }
        }.getOrNull()
    }
}
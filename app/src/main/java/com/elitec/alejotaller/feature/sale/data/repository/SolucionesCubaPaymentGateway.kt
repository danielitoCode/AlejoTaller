package com.elitec.alejotaller.feature.sale.data.repository

import android.util.Log
import com.elitec.alejotaller.BuildConfig
import com.elitec.shared.sale.feature.sale.domain.repository.PaymentGateway
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import org.json.JSONObject
import java.util.Locale

class SolucionesCubaPaymentGateway(
    private val httpClient: HttpClient
): PaymentGateway {

    private companion object {
        const val TAG = "SolucionesCubaGateway"
    }

    override suspend fun createCheckoutUrl(
        saleId: String,
        amount: Double,
        description: String
    ): Result<String> = createPaymentLink(saleId, amount, description)
    /**
     * Crea un link de pago en la pasarela de Soluciones Cuba.
     *
     * @param saleId    ID Ãºnico de la venta â€” se usa como [reference] para que la
     *                  pasarela lo devuelva en el callback y podamos identificar quÃ©
     *                  venta fue pagada.
     * @param amount    Monto total a cobrar en CUP.
     * @param description DescripciÃ³n legible del pedido (se muestra al pagador).
     *
     * @return [Result] con la URL de checkout si la llamada fue exitosa,
     *         o un error describiendo el problema.
     */
    suspend fun createPaymentLink(
        saleId: String,
        amount: Double,
        description: String
    ): Result<String> = runCatching {
        val endpoint = BuildConfig.SOLUCIONES_CUBA_PAY_API_URL
        require(endpoint.isNotBlank()) { "SOLUCIONES_CUBA_PAY_API_URL no estÃ¡ configurada" }
        require(BuildConfig.SOLUCIONES_CUBA_API_KEY.isNotBlank()) { "SOLUCIONES_CUBA_API_KEY no estÃ¡ configurada" }
        require(BuildConfig.SOLUCIONES_CUBA_MERCHANT_ID.isNotBlank()) { "SOLUCIONES_CUBA_MERCHANT_ID no estÃ¡ configurado" }
        require(amount > 0) { "El monto debe ser mayor que 0" }
        Log.i(TAG, "event=payment_checkout_start saleId=$saleId amount=$amount endpoint=$endpoint")
        val response = httpClient.submitForm(
            url = endpoint,
            formParameters = Parameters.build {
                append("api_key",     BuildConfig.SOLUCIONES_CUBA_API_KEY)
                append("merchant_id", BuildConfig.SOLUCIONES_CUBA_MERCHANT_ID)
                append("amount",      String.format(Locale.US, "%.2f", amount))
                append("currency",    "CUP")
                append("reference",   saleId)         // â† el ID de venta como referencia
                append("description", description)
                // URLs de retorno â€” deben configurarse en local.properties cuando
                // tengas el endpoint de la Appwrite Function disponible.
                // Formato recomendado para apps Android: deeplink propio de la app
                // o una URL de la Appwrite Function que actualice el estado de la venta.
                append("success_url",  BuildConfig.SOLUCIONES_CUBA_SUCCESS_URL.ifBlank { "" })
                append("cancel_url",   BuildConfig.SOLUCIONES_CUBA_CANCEL_URL.ifBlank { "" })
                append("callback_url", BuildConfig.SOLUCIONES_CUBA_CALLBACK_URL.ifBlank { "" })
            }
        )
        val payload = response.bodyAsText().trim()
        val checkoutUrl = extractCheckoutUrl(payload)
            ?: throw IllegalStateException(
                "No se pudo obtener URL de pago desde la API. Respuesta: $payload"
            )
        Log.i(TAG, "event=payment_checkout_success saleId=$saleId checkoutUrl=$checkoutUrl")
        checkoutUrl
    }.onFailure { error ->
        Log.e(TAG, "event=payment_checkout_failed saleId=$saleId cause=${error.message}", error)
    }

    /**
     * Extrae la URL de checkout del payload de respuesta.
     * Soporta dos formatos posibles:
     * 1. URL directa como string plano
     * 2. JSON con uno de los campos conocidos
     */
    private fun extractCheckoutUrl(rawPayload: String): String? {
        // Caso 1: la respuesta es directamente una URL
        val direct = rawPayload.takeIf {
            it.startsWith("http://") || it.startsWith("https://")
        }
        if (direct != null) return direct
        // Caso 2: la respuesta es JSON
        return runCatching {
            val obj = JSONObject(rawPayload)
            listOf("checkout_url", "payment_url", "redirect_url", "url", "link")
                .firstNotNullOfOrNull { key ->
                    obj.optString(key).takeIf {
                        it.startsWith("http://") || it.startsWith("https://")
                    }
                }
        }.getOrNull()
    }
}
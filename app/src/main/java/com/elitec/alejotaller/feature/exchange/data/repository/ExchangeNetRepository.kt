package com.elitec.alejotaller.feature.exchange.data.repository

import com.elitec.alejotaller.feature.exchange.data.dto.CupExchangeDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class ExchangeNetRepository(
    private val httpClient: HttpClient
) {
    private val baseUrl = "https://widgets.directoriocubano.info/api/tasas"

    suspend fun getExchangeToday(): CupExchangeDTO {
        val response = httpClient.get(baseUrl)
        if (response.status.value !in 200..299) {
            throw Exception("directorioCubano respondió ${response.status.value}: ${response.bodyAsText()}")
        }
        return response.body()
    }
}

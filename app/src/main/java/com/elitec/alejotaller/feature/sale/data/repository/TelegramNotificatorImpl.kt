package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.sale.domain.repository.TelegramNotificator
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TelegramNotificatorImpl(
    private val httpClient: HttpClient
): TelegramNotificator {
    override suspend fun notify(groupsMessages: String) {
        httpClient.post(BuildConfig.TELEGRAM_API_URL) {
            contentType(ContentType.Application.Json)
            setBody(groupsMessages)
        }
    }
}
package com.elitec.alejotaller.feature.agent.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.agent.data.dto.MistralAgentsCompleteRequest
import com.elitec.alejotaller.feature.agent.data.dto.MistralAgentsCompleteResponse
import com.elitec.alejotaller.feature.agent.data.dto.MistralChatMessageDto
import com.elitec.alejotaller.feature.agent.data.dto.MistralModelDto
import com.elitec.alejotaller.feature.agent.domain.entity.AgentConnectionResult
import com.elitec.alejotaller.feature.agent.domain.entity.AgentConnectionStatus
import com.elitec.alejotaller.feature.agent.domain.entity.AgentMessage
import com.elitec.alejotaller.feature.agent.domain.entity.AgentReply
import com.elitec.alejotaller.feature.agent.domain.entity.AgentRole
import com.elitec.alejotaller.feature.agent.domain.repository.AgentRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import java.util.UUID

/**
 * Mistral HTTP client (Fase 1).
 * - Probe: GET https://api.mistral.ai/v1/models/{model_id}
 * - Chat:  POST https://api.mistral.ai/v1/agents/completions
 *
 * API key from BuildConfig (local.properties → Gradle).
 */
class AgentMistralNetRepository(
    private val httpClient: HttpClient,
) : AgentRepository {

    private val baseUrl = "https://api.mistral.ai/v1"
    private val apiKey: String get() = BuildConfig.MISTRAL_API_KEY
    private val agentId: String get() = BuildConfig.MISTRAL_AGENT_ID
    private val modelId: String
        get() = BuildConfig.MISTRAL_MODEL_ID.ifBlank { "mistral-medium-latest" }

    override suspend fun checkConnection(): AgentConnectionResult {
        if (apiKey.isBlank()) {
            return AgentConnectionResult(
                status = AgentConnectionStatus.UNCONFIGURED,
                modelId = null,
                message = "Falta MISTRAL_API_KEY en local.properties",
            )
        }
        if (agentId.isBlank()) {
            return AgentConnectionResult(
                status = AgentConnectionStatus.UNCONFIGURED,
                modelId = null,
                message = "Falta MISTRAL_AGENT_ID en local.properties",
            )
        }

        return try {
            val response = httpClient.get("$baseUrl/models/$modelId") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
            }
            if (response.status.value !in 200..299) {
                val body = runCatching { response.bodyAsText() }.getOrDefault("")
                return AgentConnectionResult(
                    status = AgentConnectionStatus.ERROR,
                    modelId = modelId,
                    message = "Mistral models HTTP ${response.status.value}: ${body.take(180)}",
                )
            }
            val model = runCatching { response.body<MistralModelDto>() }.getOrNull()
            AgentConnectionResult(
                status = AgentConnectionStatus.OK,
                modelId = model?.id ?: modelId,
                message = "Conectado a Mistral (model=${model?.id ?: modelId}, agent=$agentId)",
            )
        } catch (e: Exception) {
            AgentConnectionResult(
                status = AgentConnectionStatus.ERROR,
                modelId = modelId,
                message = "Error de conexión Mistral: ${e.message?.take(200) ?: e::class.simpleName}",
            )
        }
    }

    override suspend fun sendMessage(
        userText: String,
        history: List<AgentMessage>,
    ): AgentReply {
        require(apiKey.isNotBlank() && agentId.isNotBlank()) {
            "Agente Mistral no configurado (MISTRAL_API_KEY / MISTRAL_AGENT_ID)"
        }

        val messages = buildList {
            history.forEach { h ->
                val role = when (h.role) {
                    AgentRole.USER -> "user"
                    AgentRole.ASSISTANT -> "assistant"
                    AgentRole.SYSTEM -> "system"
                    AgentRole.TOOL -> return@forEach
                }
                add(MistralChatMessageDto(role = role, content = h.content))
            }
            add(MistralChatMessageDto(role = "user", content = userText))
        }

        val response = httpClient.post("$baseUrl/agents/completions") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(
                MistralAgentsCompleteRequest(
                    agentId = agentId,
                    messages = messages,
                )
            )
        }

        if (response.status.value !in 200..299) {
            val errBody = runCatching { response.bodyAsText() }.getOrDefault("")
            throw IllegalStateException(
                "Mistral agents HTTP ${response.status.value}: ${errBody.take(240)}"
            )
        }

        val payload = response.body<MistralAgentsCompleteResponse>()
        val text = payload.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            .orEmpty()
            .ifBlank { "(sin respuesta)" }

        return AgentReply(
            providerId = payload.id,
            message = AgentMessage(
                id = UUID.randomUUID().toString(),
                role = AgentRole.ASSISTANT,
                content = text,
                createdAtIso = java.time.Instant.now().toString(),
            ),
        )
    }
}

package com.elitec.alejotaller.feature.agent.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MistralChatMessageDto(
    val role: String,
    val content: String,
)

@Serializable
data class MistralAgentsCompleteRequest(
    @SerialName("agent_id") val agentId: String,
    val messages: List<MistralChatMessageDto>,
)

@Serializable
data class MistralAgentsCompleteResponse(
    val id: String? = null,
    val choices: List<MistralChoiceDto>? = null,
)

@Serializable
data class MistralChoiceDto(
    val index: Int? = null,
    val message: MistralChatMessageDto? = null,
)

@Serializable
data class MistralModelDto(
    val id: String? = null,
    val objectType: String? = null,
)

package com.elitec.alejotaller.feature.agent.domain.entity

enum class AgentRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL,
}

enum class AgentConnectionStatus {
    UNKNOWN,
    OK,
    ERROR,
    UNCONFIGURED,
}

data class AgentMessage(
    val id: String,
    val role: AgentRole,
    val content: String,
    val createdAtIso: String,
)

data class AgentConnectionResult(
    val status: AgentConnectionStatus,
    val modelId: String?,
    val message: String,
)

data class AgentReply(
    val message: AgentMessage,
    val providerId: String?,
)

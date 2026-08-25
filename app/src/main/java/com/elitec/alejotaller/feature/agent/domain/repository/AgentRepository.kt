package com.elitec.alejotaller.feature.agent.domain.repository

import com.elitec.alejotaller.feature.agent.domain.entity.AgentConnectionResult
import com.elitec.alejotaller.feature.agent.domain.entity.AgentMessage
import com.elitec.alejotaller.feature.agent.domain.entity.AgentReply

interface AgentRepository {
    suspend fun checkConnection(): AgentConnectionResult

    suspend fun sendMessage(
        userText: String,
        history: List<AgentMessage> = emptyList(),
    ): AgentReply
}

package com.elitec.alejotaller.feature.agent.domain.caseUse

import com.elitec.alejotaller.feature.agent.domain.entity.AgentMessage
import com.elitec.alejotaller.feature.agent.domain.entity.AgentReply
import com.elitec.alejotaller.feature.agent.domain.repository.AgentRepository

class SendAgentMessageCaseUse(
    private val repository: AgentRepository,
) {
    suspend operator fun invoke(
        text: String,
        history: List<AgentMessage> = emptyList(),
    ): AgentReply {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "El mensaje no puede estar vacío" }
        return repository.sendMessage(trimmed, history)
    }
}

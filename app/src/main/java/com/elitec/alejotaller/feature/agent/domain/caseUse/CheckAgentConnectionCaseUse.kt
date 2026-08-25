package com.elitec.alejotaller.feature.agent.domain.caseUse

import com.elitec.alejotaller.feature.agent.domain.entity.AgentConnectionResult
import com.elitec.alejotaller.feature.agent.domain.repository.AgentRepository

class CheckAgentConnectionCaseUse(
    private val repository: AgentRepository,
) {
    suspend operator fun invoke(): AgentConnectionResult =
        repository.checkConnection()
}

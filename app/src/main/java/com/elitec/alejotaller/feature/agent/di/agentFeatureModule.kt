package com.elitec.alejotaller.feature.agent.di

import com.elitec.alejotaller.feature.agent.data.repository.AgentMistralNetRepository
import com.elitec.alejotaller.feature.agent.domain.caseUse.CheckAgentConnectionCaseUse
import com.elitec.alejotaller.feature.agent.domain.caseUse.SendAgentMessageCaseUse
import com.elitec.alejotaller.feature.agent.domain.repository.AgentRepository
import org.koin.dsl.module

val agentFeatureModule = module {
    single<AgentRepository> { AgentMistralNetRepository(get()) }

    factory { CheckAgentConnectionCaseUse(get()) }
    factory { SendAgentMessageCaseUse(get()) }
}

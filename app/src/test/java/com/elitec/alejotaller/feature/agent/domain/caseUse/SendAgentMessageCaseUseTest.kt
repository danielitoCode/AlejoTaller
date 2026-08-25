package com.elitec.alejotaller.feature.agent.domain.caseUse

import com.elitec.alejotaller.feature.agent.domain.entity.AgentMessage
import com.elitec.alejotaller.feature.agent.domain.entity.AgentReply
import com.elitec.alejotaller.feature.agent.domain.entity.AgentRole
import com.elitec.alejotaller.feature.agent.domain.repository.AgentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SendAgentMessageCaseUseTest {

    private val repo = object : AgentRepository {
        override suspend fun checkConnection() = error("not used")

        override suspend fun sendMessage(
            userText: String,
            history: List<AgentMessage>,
        ): AgentReply = AgentReply(
            providerId = "p1",
            message = AgentMessage(
                id = "m1",
                role = AgentRole.ASSISTANT,
                content = "Hola",
                createdAtIso = "2026-01-01T00:00:00Z",
            ),
        )
    }

    private val useCase = SendAgentMessageCaseUse(repo)

    @Test
    fun rejectsBlankText() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { useCase("  ") }
        }
    }

    @Test
    fun delegatesToRepository() = runTest {
        val reply = useCase("¿Qué productos hay?")
        assertEquals("Hola", reply.message.content)
    }
}

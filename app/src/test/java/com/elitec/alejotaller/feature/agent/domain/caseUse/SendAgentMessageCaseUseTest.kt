package com.elitec.alejotaller.feature.agent.domain.caseUse

import com.elitec.alejotaller.feature.agent.domain.entity.AgentMessage
import com.elitec.alejotaller.feature.agent.domain.entity.AgentReply
import com.elitec.alejotaller.feature.agent.domain.entity.AgentRole
import com.elitec.alejotaller.feature.agent.domain.repository.AgentRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * Unit tests for domain case use only (no HTTP / BuildConfig).
 * Full agent Android wiring is deferred; web takes priority for Fase 1–2.
 */
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
        try {
            useCase("  ")
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun delegatesToRepository() = runTest {
        val reply = useCase("Que productos hay?")
        assertEquals("Hola", reply.message.content)
    }
}

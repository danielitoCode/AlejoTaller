package com.elitec.shared.auth.feature.auth.domain.caseuse

import com.elitec.shared.auth.feature.auth.fakes.FakeSessionManager
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthUserCaseUseTest {

    @Test
    fun `normaliza el email antes de abrir la sesion`() = runTest {
        val sessionManager = FakeSessionManager()
        val caseUse = AuthUserCaseUse(sessionManager)

        val result = caseUse("  USER@Alejo.dev  ", "pass")

        assertTrue(result.isSuccess)
        assertEquals("USER@Alejo.dev", sessionManager.openedEmail)
        assertEquals("pass", sessionManager.openedPassword)
    }
}

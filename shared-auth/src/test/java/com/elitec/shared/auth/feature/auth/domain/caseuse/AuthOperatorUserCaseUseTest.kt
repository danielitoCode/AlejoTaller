package com.elitec.shared.auth.feature.auth.domain.caseuse

import com.elitec.shared.auth.feature.auth.fakes.FakeAccountRepository
import com.elitec.shared.auth.feature.auth.fakes.FakeSessionManager
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthOperatorUserCaseUseTest {

    @Test
    fun `allow pass a operator user role`() = runTest {
        val sessionManager = FakeSessionManager()
        val caseUse = AuthOperatorUserCaseUse(
            authUserCaseUse = AuthUserCaseUse(sessionManager),
            getCurrentUserInfoCaseUse = GetCurrentUserInfoCaseUse(FakeAccountRepository(role = "operator")),
            closeSessionCaseUse = CloseSessionCaseUse(sessionManager)
        )

        val result = caseUse("operator@alejo.dev", "pass")

        assertTrue(result.isSuccess)
        assertEquals("user-test-id", result.getOrThrow().id)
        assertEquals(0, sessionManager.closeCalls)
    }

    @Test
    fun `allow pass a admin user role so coming like administrator`() = runTest {
        val sessionManager = FakeSessionManager()
        val caseUse = AuthOperatorUserCaseUse(
            authUserCaseUse = AuthUserCaseUse(sessionManager),
            getCurrentUserInfoCaseUse = GetCurrentUserInfoCaseUse(FakeAccountRepository(role = "administrator")),
            closeSessionCaseUse = CloseSessionCaseUse(sessionManager)
        )

        val result = caseUse("admin@alejo.dev", "pass")

        assertTrue(result.isSuccess)
        assertEquals(0, sessionManager.closeCalls)
    }

    @Test
    fun `close session when rol do not access of operator`() = runTest {
        val sessionManager = FakeSessionManager()
        val caseUse = AuthOperatorUserCaseUse(
            authUserCaseUse = AuthUserCaseUse(sessionManager),
            getCurrentUserInfoCaseUse = GetCurrentUserInfoCaseUse(FakeAccountRepository(role = "viewer")),
            closeSessionCaseUse = CloseSessionCaseUse(sessionManager)
        )

        val result = caseUse("viewer@alejo.dev", "pass")

        assertTrue(result.isFailure)
        assertIs<IllegalAccessException>(result.exceptionOrNull())
        assertEquals(1, sessionManager.closeCalls)
    }
}

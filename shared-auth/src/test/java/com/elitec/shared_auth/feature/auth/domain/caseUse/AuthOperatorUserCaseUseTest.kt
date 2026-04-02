package com.elitec.shared_auth.feature.auth.domain.caseUse

import com.elitec.shared.auth.feature.auth.domain.caseuse.AuthOperatorUserCaseUse
import com.elitec.shared.auth.feature.auth.domain.caseuse.AuthUserCaseUse
import com.elitec.shared.auth.feature.auth.domain.caseuse.CloseSessionCaseUse
import com.elitec.shared.auth.feature.auth.domain.caseuse.GetCurrentUserInfoCaseUse
import com.elitec.shared_auth.fakes.feature.auth.data.AccountRepositoryImplFake
import com.elitec.shared_auth.fakes.feature.auth.data.SessionManagerImplFake
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// import kotlin.te

class AuthOperatorUserCaseUseTest {
    @Test
    fun `Auth user with not operator role`() = runTest {
        // Given
        val authUserCaseUseTest = AuthUserCaseUse(SessionManagerImplFake())
        val getCurrentUserInfoCaseUse = GetCurrentUserInfoCaseUse(
            accountRepository = AccountRepositoryImplFake(
                verified = true,
                role = "custom"
            )
        )
        val closeSessionCaseUse = CloseSessionCaseUse(SessionManagerImplFake())

        // When
        val authOperatorUserCaseUse = AuthOperatorUserCaseUse(
            authUserCaseUse = authUserCaseUseTest,
            getCurrentUserInfoCaseUse = getCurrentUserInfoCaseUse,
            closeSessionCaseUse = closeSessionCaseUse
        )

        val result = authOperatorUserCaseUse("test@gmail.com", "passTest")
        // Then
        assertIs<IllegalAccessException>(result.exceptionOrNull())
    }

    @Test
    fun `Auth user with operator role`() = runTest {
        // Given
        val authUserCaseUseTest = AuthUserCaseUse(SessionManagerImplFake())
        val getCurrentUserInfoCaseUse = GetCurrentUserInfoCaseUse(
            accountRepository = AccountRepositoryImplFake(
                userId = "user test id",
                verified = true,
                role = "operator"
            )
        )
        val closeSessionCaseUse = CloseSessionCaseUse(SessionManagerImplFake())

        // When
        val authOperatorUserCaseUse = AuthOperatorUserCaseUse(
            authUserCaseUse = authUserCaseUseTest,
            getCurrentUserInfoCaseUse = getCurrentUserInfoCaseUse,
            closeSessionCaseUse = closeSessionCaseUse
        )

        val result = authOperatorUserCaseUse("test@gmail.com", "passTest")

        // Then
        assertIs<Result<String>>(result)
        assertEquals("user test Id", result.getOrThrow().id)
    }
}
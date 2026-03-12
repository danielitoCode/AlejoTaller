package com.elitec.alejotaller.feature.auth.presentation.viewmodel

import android.content.Context
import com.elitec.alejotaller.core.MainDispatcherRule
import com.elitec.alejotaller.data.fakesRepositories.FakeAccountRepository
import com.elitec.alejotaller.data.fakesRepositories.FakeGoogleAuthProvider
import com.elitec.alejotaller.data.fakesRepositories.FakeSessionManager
import com.elitec.alejotaller.feature.auth.domain.caseuse.AuthUserCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.AuthWithGoogleCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.CloseSessionCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.CustomRegisterCaseUse
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `autUser invokes onUserLogIn on success`() = runTest {
        val sessionManager = FakeSessionManager(openedSessionId = "user-1")
        val viewModel = AuthViewModel(
            authUserCaseUse = AuthUserCaseUse(sessionManager),
            authWhitGoogleUseCase = AuthWithGoogleCaseUse(
                googleAuthProvider = FakeGoogleAuthProvider(),
                registerCaseUse = CustomRegisterCaseUse(FakeAccountRepository(), sessionManager),
                accountRepository = FakeAccountRepository(),
                sessionManager = sessionManager
            ),
            logoutUserCaseUse = CloseSessionCaseUse(sessionManager)
        )
        var loggedId: String? = null
        var error: String? = null

        viewModel.autUser("user@test.com", "pass", onUserLogIn = { loggedId = it }, onFail = { error = it })
        advanceUntilIdle()

        assertEquals("user-1", loggedId)
        assertEquals(null, error)
    }

    @Test
    fun `logoutUser invokes onFail when close session fails`() = runTest {
        val sessionManager = FakeSessionManager(closeError = IllegalStateException("cannot close"))
        val viewModel = AuthViewModel(
            authUserCaseUse = AuthUserCaseUse(sessionManager),
            authWhitGoogleUseCase = AuthWithGoogleCaseUse(
                googleAuthProvider = FakeGoogleAuthProvider(),
                registerCaseUse = CustomRegisterCaseUse(FakeAccountRepository(), sessionManager),
                accountRepository = FakeAccountRepository(),
                sessionManager = sessionManager
            ),
            logoutUserCaseUse = CloseSessionCaseUse(sessionManager)
        )
        var logoutCalled = false
        var failCalled = false

        viewModel.logoutUser(onLogout = { logoutCalled = true }, onFail = { failCalled = true })
        advanceUntilIdle()

        assertTrue(!logoutCalled)
        assertTrue(failCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `authWithGoogle invokes onUserLogIn on success`() = runTest {
        val sessionManager = FakeSessionManager(openedSessionId = "google-user")
        val viewModel = AuthViewModel(
            authUserCaseUse = AuthUserCaseUse(sessionManager),
            authWhitGoogleUseCase = AuthWithGoogleCaseUse(
                googleAuthProvider = FakeGoogleAuthProvider(),
                registerCaseUse = CustomRegisterCaseUse(FakeAccountRepository(), sessionManager),
                accountRepository = FakeAccountRepository(),
                sessionManager = sessionManager
            ),
            logoutUserCaseUse = CloseSessionCaseUse(sessionManager)
        )

        var loggedId: String? = null
        var error: String? = null

        viewModel.authWithGoogle(mockk<Context>(relaxed = true), onUserLogIn = { loggedId = it }, onFail = { error = it })
        advanceUntilIdle()

        assertEquals("google-user", loggedId)
        assertEquals(null, error)
    }
}
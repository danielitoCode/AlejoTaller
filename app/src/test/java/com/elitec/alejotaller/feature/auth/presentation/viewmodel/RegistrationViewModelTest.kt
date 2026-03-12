package com.elitec.alejotaller.feature.auth.presentation.viewmodel

import android.content.Context
import com.elitec.alejotaller.core.MainDispatcherRule
import com.elitec.alejotaller.data.fakesRepositories.FakeAccountRepository
import com.elitec.alejotaller.data.fakesRepositories.FakeGoogleAuthProvider
import com.elitec.alejotaller.data.fakesRepositories.FakeSessionManager
import com.elitec.alejotaller.feature.auth.domain.caseuse.CustomRegisterCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.RegisterWithGoogleUseCase
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `customRegister calls onUserRegister on success`() = runTest {
        val sessionManager = FakeSessionManager(openedSessionId = "registered-1")
        val viewModel = RegistrationViewModel(
            registerUserCaseUse = RegisterWithGoogleUseCase(
                googleAuthProvider = FakeGoogleAuthProvider(),
                sessionManager = sessionManager,
                accountRepository = FakeAccountRepository()
            ),
            customRegisterCaseUse = CustomRegisterCaseUse(
                accountRepository = FakeAccountRepository(),
                sessionManager = sessionManager
            )
        )

        var userId: String? = null
        var error: String? = null

        viewModel.customRegister(
            email = "new@user.dev",
            password = "pass",
            name = "Nuevo",
            onUserRegister = { userId = it },
            onFail = { error = it }
        )
        advanceUntilIdle()

        assertEquals("registered-1", userId)
        assertEquals(null, error)
    }

    @Test
    fun `registerWithGoogle calls onUserRegister with email on success`() = runTest {
        val viewModel = RegistrationViewModel(
            registerUserCaseUse = RegisterWithGoogleUseCase(
                googleAuthProvider = FakeGoogleAuthProvider(),
                sessionManager = FakeSessionManager(),
                accountRepository = FakeAccountRepository()
            ),
            customRegisterCaseUse = CustomRegisterCaseUse(
                accountRepository = FakeAccountRepository(),
                sessionManager = FakeSessionManager()
            )
        )

        var registered: String? = null

        viewModel.registerWithGoogle(
            context = mockk<Context>(relaxed = true),
            onUserRegister = { registered = it },
            onFail = { }
        )
        advanceUntilIdle()

        assertEquals("test@elitec.dev", registered)
    }

    @Test
    fun `customRegister calls onFail on error`() = runTest {
        val failingSession = FakeSessionManager(openError = IllegalStateException("boom"))
        val viewModel = RegistrationViewModel(
            registerUserCaseUse = RegisterWithGoogleUseCase(
                googleAuthProvider = FakeGoogleAuthProvider(),
                sessionManager = FakeSessionManager(),
                accountRepository = FakeAccountRepository()
            ),
            customRegisterCaseUse = CustomRegisterCaseUse(
                accountRepository = FakeAccountRepository(),
                sessionManager = failingSession
            )
        )

        var failed = false
        viewModel.customRegister("a@b.com", "p", "n", onUserRegister = {}, onFail = { failed = true })
        advanceUntilIdle()

        assertTrue(failed)
    }
}
package com.elitec.alejotaller.feature.auth.presentation.viewmodel

import com.elitec.alejotaller.core.MainDispatcherRule
import com.elitec.alejotaller.data.fakesRepositories.FakeAccountRepository
import com.elitec.alejotaller.data.fakesRepositories.FakeFileRepository
import com.elitec.alejotaller.data.fakesRepositories.FakeSessionManager
import com.elitec.alejotaller.data.fakesRepositories.defaultUser
import com.elitec.alejotaller.feature.auth.domain.caseuse.CloseSessionCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.GetCurrentUserInfoCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserNameUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPassCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhoneCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhotoUrlCaseUse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `clearMessages cleans success and error messages`() = runTest {
        val accountRepository = FakeAccountRepository(currentUser = defaultUser())
        val viewModel = createViewModel(accountRepository = accountRepository)

        viewModel.getAccountInfo(onGetInfo = {}, onFail = {})
        advanceUntilIdle()
        viewModel.clearMessages()

        assertNull(viewModel.uiState.value.saveMessage)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `updateName delegates to case use`() = runTest {
        val accountRepository = FakeAccountRepository(currentUser = defaultUser())
        val viewModel = createViewModel(accountRepository = accountRepository)

        var success = false
        var fail = false
        viewModel.updateName("Otro Nombre", onNameUpdate = { success = true }, onFail = { fail = true })
        advanceUntilIdle()

        assertTrue(success)
        assertTrue(!fail)
        assertEquals(listOf("Otro Nombre"), accountRepository.updatedNames)
    }

    @Test
    fun `getAccountInfo updates state and callback with user id`() = runTest {
        val expected = defaultUser().copy(id = "abc-1")
        val accountRepository = FakeAccountRepository(currentUser = expected)
        val viewModel = createViewModel(accountRepository = accountRepository)

        var callbackId: String? = null
        var failCalled = false

        viewModel.getAccountInfo(onGetInfo = { callbackId = it }, onFail = { failCalled = true })
        advanceUntilIdle()

        assertEquals("abc-1", callbackId)
        assertTrue(!failCalled)
        assertEquals(expected, viewModel.userProfile.value)
    }

    @Test
    fun `getAccountInfo closes session and fails when repository throws`() = runTest {
        val sessionManager = FakeSessionManager()
        val accountRepository = FakeAccountRepository(
            currentUser = defaultUser(),
            getCurrentUserError = IllegalStateException("no-user")
        )
        val viewModel = createViewModel(accountRepository = accountRepository, sessionManager = sessionManager)

        var failCalled = false
        viewModel.getAccountInfo(onGetInfo = {}, onFail = { failCalled = true })
        advanceUntilIdle()

        assertTrue(failCalled)
        assertEquals(1, sessionManager.closeCalls)
    }

    private fun createViewModel(
        accountRepository: FakeAccountRepository,
        sessionManager: FakeSessionManager = FakeSessionManager()
    ): ProfileViewModel = ProfileViewModel(
        getProfileUseCase = GetCurrentUserInfoCaseUse(accountRepository),
        updatePhotoUrlCaseUse = UpdateUserPhotoUrlCaseUse(accountRepository, FakeFileRepository()),
        closeCurrentSession = CloseSessionCaseUse(sessionManager),
        updateNameCaseUse = UpdateUserNameUseCase(accountRepository),
        updateUserPassCaseUse = UpdateUserPassCaseUse(accountRepository),
        updatePhoneCaseUse = UpdateUserPhoneCaseUse(accountRepository),
    )
}
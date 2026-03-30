package com.elitec.alejotaller.feature.auth.domain.caseuse

import android.content.Context
import com.elitec.alejotaller.data.fakesRepositories.FakeAccountRepository
import com.elitec.alejotaller.data.fakesRepositories.FakeGoogleAuthProvider
import com.elitec.alejotaller.data.fakesRepositories.FakeSessionManager
import com.elitec.alejotaller.data.fakesRepositories.defaultUser
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthCaseUsesTest {

    @Test
    fun `AuthUserCaseUse returns session id when open session succeeds`() = runTest {
        val sessionManager = FakeSessionManager(openedSessionId = "session-123")
        val caseUse = AuthUserCaseUse(sessionManager)

        val result = caseUse("user@test.com", "secret")

        assertTrue(result.isSuccess)
        assertEquals("session-123", result.getOrNull())
        assertEquals(listOf("user@test.com" to "secret"), sessionManager.openCalls)
    }

    @Test
    fun `CustomRegisterCaseUse creates user and opens session`() = runTest {
        val accountRepository = FakeAccountRepository()
        val sessionManager = FakeSessionManager(openedSessionId = "session-custom")
        val caseUse = CustomRegisterCaseUse(accountRepository, sessionManager)

        val result = caseUse("new@test.com", "pass-1", "Nuevo")

        assertTrue(result.isSuccess)
        assertEquals("session-custom", result.getOrNull())
        assertEquals(1, accountRepository.createdUsers.size)
        assertEquals("new@test.com", accountRepository.createdUsers.first().email)
        assertEquals(listOf("new@test.com" to "pass-1"), sessionManager.openCalls)
    }

    @Test
    fun `CloseSessionCaseUse delegates to session manager`() = runTest {
        val sessionManager = FakeSessionManager()
        val caseUse = CloseSessionCaseUse(sessionManager)

        val result = caseUse()

        assertTrue(result.isSuccess)
        assertEquals(1, sessionManager.closeCalls)
    }

    @Test
    fun `GetCurrentUserInfoCaseUse returns current user`() = runTest {
        val expectedUser = defaultUser().copy(id = "user-55")
        val accountRepository = FakeAccountRepository(currentUser = expectedUser)
        val caseUse = GetCurrentUserInfoCaseUse(accountRepository)

        val result = caseUse()

        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }

    @Test
    fun `Update user case uses delegate to account repository`() = runTest {
        val accountRepository = FakeAccountRepository()

        UpdateUserNameUseCase(accountRepository)("Pepe")
        UpdateUserPassCaseUse(accountRepository)("super-pass")
        UpdateUserPhoneCaseUse(accountRepository)("+5355000000")

        assertEquals(listOf("Pepe"), accountRepository.updatedNames)
        assertEquals(listOf("super-pass"), accountRepository.updatedPasses)
        assertEquals(listOf("+5355000000"), accountRepository.updatedPhones)
    }

    @Test
    fun `RegisterWithGoogleUseCase creates account opens session and updates profile`() = runTest {
        val googleProvider = FakeGoogleAuthProvider()
        val sessionManager = FakeSessionManager(openedSessionId = "session-google")
        val accountRepository = FakeAccountRepository()
        val caseUse = RegisterWithGoogleUseCase(googleProvider, sessionManager, accountRepository)

        val result = caseUse(mockk<Context>(relaxed = true))

        assertTrue(result.isSuccess)
        assertEquals(googleProvider.user.email, result.getOrNull())
        assertEquals(1, accountRepository.createdUsers.size)
        assertEquals(1, accountRepository.updatedProfiles.size)
        assertEquals(1, sessionManager.openCalls.size)
    }

    @Test
    fun `AuthWithGoogleCaseUse opens session for existing user without creating account`() = runTest {
        val googleProvider = FakeGoogleAuthProvider()
        val accountRepository = FakeAccountRepository()
        val sessionManager = FakeSessionManager(openedSessionId = "existing-user-id")
        val registerUseCase = CustomRegisterCaseUse(accountRepository, sessionManager)
        val caseUse = AuthWithGoogleCaseUse(
            googleAuthProvider = googleProvider,
            registerCaseUse = registerUseCase,
            accountRepository = accountRepository,
            sessionManager = sessionManager
        )

        val result = caseUse(mockk<Context>(relaxed = true))

        assertTrue(result.isSuccess)
        assertEquals("existing-user-id", result.getOrNull())
        assertEquals(0, accountRepository.createdUsers.size)
        assertEquals(0, accountRepository.updatedProfiles.size)
        assertEquals(1, sessionManager.closeCalls)
        assertEquals(1, sessionManager.openCalls.size)
    }

    @Test
    fun `AuthWithGoogleCaseUse syncs google photo only when current profile has no photo`() = runTest {
        val googleProvider = FakeGoogleAuthProvider().apply {
            user = user.copy(photoUrl = "https://cdn.example.com/google-photo.jpg")
        }
        val accountRepository = FakeAccountRepository(
            currentUser = defaultUser().copy(
                userProfile = defaultUser().userProfile.copy(photoUrl = "")
            )
        )
        val sessionManager = FakeSessionManager(openedSessionId = "existing-user-id")
        val registerUseCase = CustomRegisterCaseUse(accountRepository, sessionManager)
        val caseUse = AuthWithGoogleCaseUse(
            googleAuthProvider = googleProvider,
            registerCaseUse = registerUseCase,
            accountRepository = accountRepository,
            sessionManager = sessionManager
        )

        val result = caseUse(mockk<Context>(relaxed = true))

        assertTrue(result.isSuccess)
        assertEquals(listOf("https://cdn.example.com/google-photo.jpg"), accountRepository.updatedPhotoUrls)
    }

    @Test
    fun `AuthWithGoogleCaseUse does not overwrite an existing custom photo with google photo`() = runTest {
        val googleProvider = FakeGoogleAuthProvider().apply {
            user = user.copy(photoUrl = "https://cdn.example.com/google-photo.jpg")
        }
        val accountRepository = FakeAccountRepository(
            currentUser = defaultUser().copy(
                userProfile = defaultUser().userProfile.copy(photoUrl = "https://cdn.example.com/custom-photo.jpg")
            )
        )
        val sessionManager = FakeSessionManager(openedSessionId = "existing-user-id")
        val registerUseCase = CustomRegisterCaseUse(accountRepository, sessionManager)
        val caseUse = AuthWithGoogleCaseUse(
            googleAuthProvider = googleProvider,
            registerCaseUse = registerUseCase,
            accountRepository = accountRepository,
            sessionManager = sessionManager
        )

        val result = caseUse(mockk<Context>(relaxed = true))

        assertTrue(result.isSuccess)
        assertTrue(accountRepository.updatedPhotoUrls.isEmpty())
    }

    @Test
    fun `AuthWithGoogleCaseUse ignores close session failure and still opens the new session`() = runTest {
        val googleProvider = FakeGoogleAuthProvider()
        val accountRepository = FakeAccountRepository()
        val sessionManager = FakeSessionManager(
            openedSessionId = "existing-user-id",
            closeError = IllegalStateException("session already closed")
        )
        val registerUseCase = CustomRegisterCaseUse(accountRepository, sessionManager)
        val caseUse = AuthWithGoogleCaseUse(
            googleAuthProvider = googleProvider,
            registerCaseUse = registerUseCase,
            accountRepository = accountRepository,
            sessionManager = sessionManager
        )

        val result = caseUse(mockk<Context>(relaxed = true))

        assertTrue(result.isSuccess)
        assertEquals("existing-user-id", result.getOrNull())
        assertEquals(1, sessionManager.closeCalls)
        assertEquals(1, sessionManager.openCalls.size)
    }

    @Test
    fun `AuthUserCaseUse returns failure when session manager throws`() = runTest {
        val sessionManager = FakeSessionManager(openError = IllegalStateException("no session"))
        val caseUse = AuthUserCaseUse(sessionManager)

        val result = caseUse("a@b.com", "p")

        assertTrue(result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
    }
}

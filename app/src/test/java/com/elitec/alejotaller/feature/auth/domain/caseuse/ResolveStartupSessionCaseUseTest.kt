package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.data.fakesRepositories.FakeAccountRepository
import com.elitec.alejotaller.data.fakesRepositories.FakeSessionManager
import com.elitec.alejotaller.data.fakesRepositories.defaultUser
import com.elitec.alejotaller.feature.auth.domain.entity.User
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.ports.FirstVisitStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class InMemoryFirstVisitStore(
    var visited: Boolean = false
) : FirstVisitStore {
    override fun hasCompletedWelcome(): Boolean = visited
    override fun markWelcomeCompleted() { visited = true }
    override fun clear() { visited = false }
}

class ResolveStartupSessionCaseUseTest {

    private fun useCase(
        account: FakeAccountRepository = FakeAccountRepository(),
        session: FakeSessionManager = FakeSessionManager(),
        firstVisit: InMemoryFirstVisitStore = InMemoryFirstVisitStore()
    ) = ResolveStartupSessionCaseUse(
        getCurrentUserInfo = GetCurrentUserInfoCaseUse(account),
        sessionManager = session,
        firstVisitStore = firstVisit
    )

    @Test
    fun clearProfile_returnsAuthenticated() = runTest {
        val account = FakeAccountRepository(currentUser = defaultUser())
        val firstVisit = InMemoryFirstVisitStore()
        val result = useCase(account = account, firstVisit = firstVisit)()
        assertTrue(result is ResolveStartupSessionCaseUse.Outcome.Authenticated)
        assertEquals(defaultUser().id, (result as ResolveStartupSessionCaseUse.Outcome.Authenticated).userId)
        assertTrue(firstVisit.visited)
    }

    @Test
    fun anonymousProfile_returnsVisitor() = runTest {
        val anon = User(
            id = "anon-1",
            name = "",
            email = "",
            pass = "",
            userProfile = UserProfile("", "", "", false)
        )
        val account = FakeAccountRepository(currentUser = anon)
        val result = useCase(account = account)()
        assertTrue(result is ResolveStartupSessionCaseUse.Outcome.Visitor)
        assertEquals("anon-1", (result as ResolveStartupSessionCaseUse.Outcome.Visitor).userId)
    }

    @Test
    fun noSession_firstVisit_showsWelcome() = runTest {
        val account = FakeAccountRepository(getError = IllegalStateException("no session"))
        val firstVisit = InMemoryFirstVisitStore(visited = false)
        val result = useCase(account = account, firstVisit = firstVisit)()
        assertEquals(ResolveStartupSessionCaseUse.Outcome.ShowWelcome, result)
    }

    @Test
    fun noSession_returning_opensAnonymousVisitor() = runTest {
        val account = FakeAccountRepository(getError = IllegalStateException("no session"))
        val session = FakeSessionManager(openedSessionId = "guest-99")
        val firstVisit = InMemoryFirstVisitStore(visited = true)
        val result = useCase(account = account, session = session, firstVisit = firstVisit)()
        assertTrue(result is ResolveStartupSessionCaseUse.Outcome.Visitor)
        assertEquals("guest-99", (result as ResolveStartupSessionCaseUse.Outcome.Visitor).userId)
        assertEquals(1, session.anonymousOpenCalls)
    }

    @Test
    fun noSession_deeplink_opensAnonymousVisitor() = runTest {
        val account = FakeAccountRepository(getError = IllegalStateException("no session"))
        val session = FakeSessionManager(openedSessionId = "guest-deeplink")
        val firstVisit = InMemoryFirstVisitStore(visited = false)
        val result = useCase(account = account, session = session, firstVisit = firstVisit)(
            hasProductDeeplink = true
        )
        assertTrue(result is ResolveStartupSessionCaseUse.Outcome.Visitor)
        assertEquals("guest-deeplink", (result as ResolveStartupSessionCaseUse.Outcome.Visitor).userId)
    }
}

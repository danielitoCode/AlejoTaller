package com.elitec.alejotaller.feature.auth.domain.caseuse

import com.elitec.alejotaller.feature.auth.domain.ports.FirstVisitStore
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import com.elitec.alejotaller.feature.auth.domain.util.SessionMode
import com.elitec.alejotaller.feature.auth.domain.util.classifySessionMode
import com.elitec.alejotaller.feature.auth.domain.util.hasClearAuthenticatedProfile

/**
 * Startup decision per AUTH_POLICY:
 * - clear profile → authenticated home
 * - unclear / anonymous Appwrite session → visitor home
 * - no session + deeplink or returning → create anonymous → visitor home
 * - no session + first visit → Welcome (Landing)
 */
class ResolveStartupSessionCaseUse(
    private val getCurrentUserInfo: GetCurrentUserInfoCaseUse,
    private val sessionManager: SessionManager,
    private val firstVisitStore: FirstVisitStore
) {

    sealed class Outcome {
        data class Authenticated(val userId: String) : Outcome()
        data class Visitor(val userId: String) : Outcome()
        data object ShowWelcome : Outcome()
    }

    suspend operator fun invoke(hasProductDeeplink: Boolean = false): Outcome {
        val current = getCurrentUserInfo()
        if (current.isSuccess) {
            val user = current.getOrThrow()
            return if (hasClearAuthenticatedProfile(user)) {
                firstVisitStore.markWelcomeCompleted()
                Outcome.Authenticated(user.id)
            } else {
                firstVisitStore.markWelcomeCompleted()
                Outcome.Visitor(user.id)
            }
        }

        // No usable session
        if (hasProductDeeplink || firstVisitStore.hasCompletedWelcome()) {
            return openVisitorSession()
        }
        return Outcome.ShowWelcome
    }

    private suspend fun openVisitorSession(): Outcome {
        return runCatching {
            val userId = sessionManager.openAnonymousSession()
            firstVisitStore.markWelcomeCompleted()
            Outcome.Visitor(userId)
        }.getOrElse {
            Outcome.ShowWelcome
        }
    }
}

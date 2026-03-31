package com.elitec.shared.auth.feature.auth.domain.caseuse

import com.elitec.shared.auth.feature.auth.domain.entity.User

class AuthOperatorUserCaseUse(
    private val authUserCaseUse: AuthUserCaseUse,
    private val getCurrentUserInfoCaseUse: GetCurrentUserInfoCaseUse,
    private val closeSessionCaseUse: CloseSessionCaseUse
) {
    suspend operator fun invoke(email: String, pass: String): Result<User> = runCatching {
        authUserCaseUse(email, pass).getOrThrow()

        val currentUser = getCurrentUserInfoCaseUse().getOrThrow()
        val normalizedRole = currentUser.userProfile.role?.trim()?.lowercase()

        if (normalizedRole != "operator" && normalizedRole != "admin") {
            closeSessionCaseUse()
            throw IllegalAccessException("Solo operadores autorizados pueden acceder a esta aplicacion.")
        }

        currentUser
    }
}

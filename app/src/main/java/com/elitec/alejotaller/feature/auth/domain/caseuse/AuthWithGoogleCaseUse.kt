package com.elitec.alejotaller.feature.auth.domain.caseuse

import android.content.Context
import com.elitec.alejotaller.feature.auth.domain.caseuse.util.hashEmailWithSub
import com.elitec.alejotaller.feature.auth.domain.entity.UserProfile
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider
import com.elitec.alejotaller.feature.auth.domain.ports.SessionManager
import com.elitec.alejotaller.feature.auth.domain.repositories.AccountRepository
import io.appwrite.exceptions.AppwriteException

class AuthWithGoogleCaseUse(
    private val googleAuthProvider: GoogleAuthProvider,
    private val registerCaseUse: CustomRegisterCaseUse,
    private val accountRepository: AccountRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(context: Context): Result<String> = runCatching {
        val googleUser = googleAuthProvider.getUser(context)

        val password = hashEmailWithSub(
            email = googleUser.email,
            sub = googleUser.sub
        )

        return@runCatching try {
            sessionManager.openEmailSession(googleUser.email, password)
        } catch (e: Exception) {
            if(e !is AppwriteException) throw e

            when(e.code) {
                401 -> {
                    registerCaseUse(
                        email = googleUser.email,
                        password = password,
                        name = googleUser.name
                    )
                    val userId = sessionManager.openEmailSession(googleUser.email, password)
                    accountRepository.updateProfile(
                        UserProfile(
                            sub = googleUser.sub,
                            phone = googleUser.phone,
                            photoUrl = googleUser.photoUrl,
                            verification = false
                        )
                    )
                    userId
                }
                else -> throw e
            }
        }
    }
}
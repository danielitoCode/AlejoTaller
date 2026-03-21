package com.elitec.alejotaller.feature.auth.domain.caseuse

import android.content.Context
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
        val password = googleUser.sub

        runCatching { sessionManager.closeCurrentSession() }

        return@runCatching try {
            sessionManager.openEmailSession(googleUser.email, password)
        } catch (e: Exception) {
            if (e !is AppwriteException) throw e

            when(e.code) {
                401 -> {
                    val userId = try {
                        registerCaseUse(
                            email = googleUser.email,
                            password = password,
                            name = googleUser.name
                        ).getOrElse { throw it }
                    } catch (registerError: Exception) {
                        val appwriteError = registerError as? AppwriteException
                        if (appwriteError?.code == 409) {
                            throw IllegalStateException(
                                "Tu contraseña fue cambiada. " +
                                        "Inicia una vez con correo y contraseña para continuar con Google."
                            )
                        }
                        throw registerError
                    }

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

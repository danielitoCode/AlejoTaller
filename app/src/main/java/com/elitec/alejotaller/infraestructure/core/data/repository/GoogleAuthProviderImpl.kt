package com.elitec.alejotaller.infraestructure.core.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CredentialManager
import com.elitec.alejotaller.infraestructure.core.domain.entity.GoogleUser
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleAuthProviderImpl(
    private val context: Context
): GoogleAuthProvider {
    override suspend fun getUser(): GoogleUser {
        val hashNonce =  createNonce()

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Query all google accounts on the device
            .setServerClientId("339467923021-iu6o7uv9p7a1d7mpt26bon6mnhf9h902.apps.googleusercontent.com") // Web client
            .setNonce(hashNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        val credential = credentialManager.getCredential(context, request).credential

        // Paso 2: Extraer información desde la credencial
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val googleIdToken = googleIdTokenCredential.idToken

        Log.d("GoogleSignIn", "Token obtenido correctamente: ${googleIdToken.take(25)}...")

        // Paso 3: Decodificar token
        val decodedToken = decodeGoogleIdToken(idToken = googleIdToken)
            ?: throw Exception("Error al decodificar token de Google")

        val email = decodedToken.getString("email")
            ?: throw Exception("No se encontró un email en la cuenta de Google")

        val sub = decodedToken.getString("sub")
            ?: throw Exception("No se pudo obtener el identificador de Google (sub)")

        val name = googleIdTokenCredential.displayName
            ?: throw Exception("Tu cuenta de Google no tiene un nombre configurado")

        val photoUrl = googleIdTokenCredential.profilePictureUri?.toString().orEmpty()
        val phone = googleIdTokenCredential.phoneNumber.orEmpty()

        return GoogleUser(email, name, sub, photoUrl, phone)
    }
}
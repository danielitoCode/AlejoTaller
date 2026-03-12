package com.elitec.alejotaller.data.fakesRepositories

import android.content.Context
import com.elitec.alejotaller.feature.auth.domain.entity.GoogleUser
import com.elitec.alejotaller.feature.auth.domain.ports.GoogleAuthProvider

class FakeGoogleAuthProvider(
    var user: GoogleUser = GoogleUser(
        email = "test@elitec.dev",
        name = "Test",
        sub = "sub-1",
        photoUrl = "https://img",
        phone = "+53"
    )
) : GoogleAuthProvider {
    override suspend fun getUser(context: Context): GoogleUser = user
}
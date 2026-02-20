package com.elitec.alejotaller.feature.auth.domain.ports

import android.content.Context
import com.elitec.alejotaller.feature.auth.domain.entity.GoogleUser

interface GoogleAuthProvider {
    suspend fun getUser(context: Context): GoogleUser
}
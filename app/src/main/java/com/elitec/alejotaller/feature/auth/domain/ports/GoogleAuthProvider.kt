package com.elitec.alejotaller.feature.auth.domain.ports

import com.elitec.alejotaller.infraestructure.core.domain.entity.GoogleUser

interface GoogleAuthProvider {
    suspend fun getUser(): GoogleUser
}
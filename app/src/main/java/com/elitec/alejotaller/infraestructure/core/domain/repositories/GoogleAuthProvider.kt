package com.elitec.alejotaller.infraestructure.core.domain.repositories

import com.elitec.alejotaller.infraestructure.core.domain.entity.GoogleUser

interface GoogleAuthProvider {
    suspend fun getUser(): GoogleUser
}
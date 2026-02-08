package com.elitec.alejotaller.infraestructure.core.domain.entity

data class GoogleUser(
    val email: String,
    val name: String,
    val sub: String,
    val photoUrl: String?,
    val phone: String?
)
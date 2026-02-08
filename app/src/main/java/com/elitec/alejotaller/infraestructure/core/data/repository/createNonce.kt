package com.elitec.alejotaller.infraestructure.core.data.repository

import java.security.MessageDigest
import java.util.UUID

fun createNonce(): String {
    val rawNonce = UUID.randomUUID().toString()
    val byte = rawNonce.toByteArray()

    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(byte)

    return digest.fold("") { str, it ->
        str + "%02x".format(it)
    }
}
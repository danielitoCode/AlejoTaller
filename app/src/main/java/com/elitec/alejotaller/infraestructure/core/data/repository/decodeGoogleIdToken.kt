package com.elitec.alejotaller.infraestructure.core.data.repository

import android.util.Base64
import org.json.JSONObject

fun decodeGoogleIdToken(idToken: String): JSONObject? {
    return try {
        val parts = idToken.split(".")
        if (parts.size == 3) {
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            JSONObject(payload)
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
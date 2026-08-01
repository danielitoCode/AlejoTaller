package com.elitec.alejotaller.feature.auth.data

import android.content.Context
import com.elitec.alejotaller.feature.auth.domain.ports.FirstVisitStore

class FirstVisitPreferences(
    context: Context
) : FirstVisitStore {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun hasCompletedWelcome(): Boolean =
        prefs.getBoolean(KEY_HAS_VISITED, false)

    override fun markWelcomeCompleted() {
        prefs.edit().putBoolean(KEY_HAS_VISITED, true).apply()
    }

    override fun clear() {
        prefs.edit().remove(KEY_HAS_VISITED).apply()
    }

    companion object {
        private const val PREFS_NAME = "alejo_auth_prefs"
        private const val KEY_HAS_VISITED = "alejo_has_visited"
    }
}

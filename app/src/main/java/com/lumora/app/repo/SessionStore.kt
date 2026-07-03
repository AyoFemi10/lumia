package com.lumora.app.repo

import android.content.Context
import android.content.SharedPreferences

data class Session(val token: String, val userId: String, val username: String, val displayName: String)

class SessionStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lumia_session", Context.MODE_PRIVATE)

    fun save(session: Session) {
        prefs.edit()
            .putString("token", session.token)
            .putString("userId", session.userId)
            .putString("username", session.username)
            .putString("displayName", session.displayName)
            .apply()
    }

    fun load(): Session? {
        val token = prefs.getString("token", null) ?: return null
        val userId = prefs.getString("userId", null) ?: return null
        val username = prefs.getString("username", null) ?: return null
        val displayName = prefs.getString("displayName", null) ?: username
        return Session(token, userId, username, displayName)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

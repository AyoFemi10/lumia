package com.lumora.app.repo

class AuthRepository(private val sessionStore: SessionStore) {

    suspend fun register(username: String, password: String, displayName: String): Result<Session> {
        return try {
            val res = ApiClient.api.register(AuthRequest(username, password, displayName))
            val session = Session(res.token, res.userId, res.username, res.displayName)
            sessionStore.save(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<Session> {
        return try {
            val res = ApiClient.api.login(AuthRequest(username, password))
            val session = Session(res.token, res.userId, res.username, res.displayName)
            sessionStore.save(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun currentSession(): Session? = sessionStore.load()

    fun logout() = sessionStore.clear()
}

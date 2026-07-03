package com.lumora.app.repo

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// TODO: point this at wherever backend/server.js is running — see backend/README.md.
// - Server running in Termux on THIS SAME phone: "http://127.0.0.1:4000/"
// - Server running on another device on your Wi-Fi (PC, another phone): use its LAN IP,
//   e.g. "http://192.168.1.42:4000/"
// - Android Studio emulator only: "http://10.0.2.2:4000/"
const val BASE_URL = "http://127.0.0.1:4000/"

data class AuthRequest(val username: String, val password: String, val displayName: String? = null)
data class AuthResponse(val token: String, val userId: String, val username: String, val displayName: String)
data class UserSummary(val id: String, val username: String, val display_name: String)
data class StartChatRequest(val username: String)
data class StartChatResponse(val chatId: String, val participant: UserSummary)
data class ChatThreadDto(
    val chatId: String,
    val participant: UserSummary?,
    val lastMessage: String,
    val lastMessageTime: Long
)
data class MessageDto(
    val id: String,
    val chat_id: String,
    val sender_id: String,
    val text: String,
    val created_at: Long
)

interface LumiaApi {
    @POST("auth/register")
    suspend fun register(@Body body: AuthRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: AuthRequest): AuthResponse

    @GET("users/search")
    suspend fun searchUsers(@Header("Authorization") bearer: String, @Query("q") q: String): List<UserSummary>

    @GET("chats")
    suspend fun getChats(@Header("Authorization") bearer: String): List<ChatThreadDto>

    @POST("chats/start")
    suspend fun startChat(@Header("Authorization") bearer: String, @Body body: StartChatRequest): StartChatResponse

    @GET("chats/{chatId}/messages")
    suspend fun getMessages(@Header("Authorization") bearer: String, @Path("chatId") chatId: String): List<MessageDto>
}

object ApiClient {
    val api: LumiaApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LumiaApi::class.java)
    }
}

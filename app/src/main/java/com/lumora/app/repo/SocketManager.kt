package com.lumora.app.repo

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {
    private var socket: Socket? = null

    fun connect(token: String) {
        if (socket?.connected() == true) return

        val opts = IO.Options().apply {
            auth = mapOf("token" to token)
            reconnection = true
        }
        socket = IO.socket(BASE_URL.trimEnd('/'), opts)
        socket?.connect()
    }

    fun joinChat(chatId: String) {
        socket?.emit("join_chat", chatId)
    }

    fun sendMessage(chatId: String, text: String) {
        val payload = JSONObject().apply {
            put("chatId", chatId)
            put("text", text)
        }
        socket?.emit("send_message", payload)
    }

    fun onNewMessage(listener: (MessageDto) -> Unit) {
        socket?.on("new_message") { args ->
            val obj = args.getOrNull(0) as? JSONObject ?: return@on
            val msg = MessageDto(
                id = obj.optString("id"),
                chat_id = obj.optString("chat_id"),
                sender_id = obj.optString("sender_id"),
                text = obj.optString("text"),
                created_at = obj.optLong("created_at")
            )
            listener(msg)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }
}

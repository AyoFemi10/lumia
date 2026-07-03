package com.lumora.app.model

data class LumiaUser(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val followerCount: Int,
    val isVerified: Boolean = false
)

data class VideoPost(
    val id: String,
    val creator: LumiaUser,
    val videoUrl: String,
    val thumbnailUrl: String,
    val caption: String,
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int,
    val soundName: String
)

enum class MessageStatus { SENT, DELIVERED, READ }

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT,
    val isMine: Boolean = false
)

data class ChatThread(
    val id: String,
    val participant: LumiaUser,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)

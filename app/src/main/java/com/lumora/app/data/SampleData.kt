package com.lumora.app.data

import com.lumora.app.model.ChatMessage
import com.lumora.app.model.ChatThread
import com.lumora.app.model.LumiaUser
import com.lumora.app.model.MessageStatus
import com.lumora.app.model.VideoPost

/**
 * Mock in-memory data source.
 * Replace with real API/Firebase/WebSocket calls once the backend exists.
 * Sample videos are Google's public test clips so the feed actually plays.
 */
object SampleData {

    private val creators = listOf(
        LumiaUser("u1", "kingsley.dev", "Kingsley", "https://i.pravatar.cc/150?img=12", 18400, true),
        LumiaUser("u2", "naija.tech", "Naija Tech", "https://i.pravatar.cc/150?img=32", 9200, false),
        LumiaUser("u3", "footy.clips", "Footy Clips", "https://i.pravatar.cc/150?img=45", 54000, true)
    )

    val feed = listOf(
        VideoPost(
            id = "v1",
            creator = creators[0],
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            thumbnailUrl = "https://i.imgur.com/3ZQ3Z4x.png",
            caption = "Building Lumia from my phone \uD83D\uDCF1\uD83D\uDD25 #buildinpublic",
            likeCount = 1240,
            commentCount = 88,
            shareCount = 41,
            soundName = "Original sound - kingsley.dev"
        ),
        VideoPost(
            id = "v2",
            creator = creators[1],
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            thumbnailUrl = "https://i.imgur.com/3ZQ3Z4x.png",
            caption = "This mobile dev setup is unreal",
            likeCount = 860,
            commentCount = 32,
            shareCount = 10,
            soundName = "Lo-fi beats vol. 3"
        ),
        VideoPost(
            id = "v3",
            creator = creators[2],
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://i.imgur.com/3ZQ3Z4x.png",
            caption = "That finish though \u26BD\uFE0F\uD83D\uDD25",
            likeCount = 20300,
            commentCount = 940,
            shareCount = 512,
            soundName = "Crowd roar"
        )
    )

    val chatThreads = listOf(
        ChatThread("c1", creators[1], "yo the launcher build is fire", "2m", unreadCount = 2),
        ChatThread("c2", creators[2], "sent you the clip", "1h", unreadCount = 0),
        ChatThread("c3", creators[0], "let's collab on the next drop", "yesterday", unreadCount = 0)
    )

    fun messagesFor(threadId: String): List<ChatMessage> = listOf(
        ChatMessage("m1", "them", "Hey! Saw the Lumia teaser \uD83D\uDD25", 0, MessageStatus.READ, isMine = false),
        ChatMessage("m2", "me", "Appreciate that, still early days", 0, MessageStatus.READ, isMine = true),
        ChatMessage("m3", "them", "Telegram + TikTok in one app is huge if you pull it off", 0, MessageStatus.DELIVERED, isMine = false),
        ChatMessage("m4", "me", "That's the plan, building it solo on mobile", 0, MessageStatus.SENT, isMine = true)
    )

    val currentUser = LumiaUser("me", "you", "You", "https://i.pravatar.cc/150?img=68", 340)
}

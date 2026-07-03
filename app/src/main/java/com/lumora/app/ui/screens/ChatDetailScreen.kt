package com.lumora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumora.app.repo.ApiClient
import com.lumora.app.repo.MessageDto
import com.lumora.app.repo.SessionStore
import com.lumora.app.repo.SocketManager
import com.lumora.app.ui.theme.LumiaPrimary
import com.lumora.app.ui.theme.LumiaSurfaceAlt
import kotlinx.coroutines.launch

@Composable
fun ChatDetailScreen(chatId: String, title: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionStore(context).load() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Load history, connect socket, join room, listen for new messages.
    LaunchedEffect(chatId) {
        val token = session?.token ?: return@LaunchedEffect
        try {
            messages = ApiClient.api.getMessages("Bearer $token", chatId)
            error = null
        } catch (e: Exception) {
            error = "Couldn't load history — is the server running?"
        }

        SocketManager.connect(token)
        SocketManager.joinChat(chatId)
        SocketManager.onNewMessage { msg ->
            if (msg.chat_id == chatId) {
                messages = messages + msg
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...") },
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (input.isNotBlank()) {
                            SocketManager.sendMessage(chatId, input)
                            input = ""
                        }
                    }) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = LumiaPrimary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp)
        ) {
            items(messages) { msg -> MessageBubble(msg, isMine = msg.sender_id == session?.userId) }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }
}

@Composable
private fun MessageBubble(msg: MessageDto, isMine: Boolean) {
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isMine) LumiaPrimary else LumiaSurfaceAlt

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(msg.text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

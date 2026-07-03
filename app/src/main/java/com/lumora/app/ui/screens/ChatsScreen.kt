package com.lumora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumora.app.repo.ApiClient
import com.lumora.app.repo.ChatThreadDto
import com.lumora.app.repo.SessionStore
import com.lumora.app.repo.StartChatRequest
import com.lumora.app.ui.theme.LumiaSurfaceAlt
import kotlinx.coroutines.launch

@Composable
fun ChatsScreen(onOpenChat: (String, String) -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionStore(context).load() }
    val scope = rememberCoroutineScope()

    var threads by remember { mutableStateOf<List<ChatThreadDto>>(emptyList()) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    fun reload() {
        val token = session?.token ?: return
        scope.launch {
            try {
                threads = ApiClient.api.getChats("Bearer $token")
                loadError = null
            } catch (e: Exception) {
                loadError = "Couldn't reach server — check it's running and BASE_URL is correct."
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lumia", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showNewChatDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "New chat")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            loadError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (threads.isEmpty() && loadError == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No chats yet — tap the pencil icon to start one.")
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(threads) { thread ->
                    ChatRow(thread = thread, onClick = {
                        val name = thread.participant?.display_name ?: thread.participant?.username ?: "Chat"
                        onOpenChat(thread.chatId, name)
                    })
                }
            }
        }
    }

    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { showNewChatDialog = false },
            onStart = { username ->
                val token = session?.token ?: return@NewChatDialog
                scope.launch {
                    try {
                        val res = ApiClient.api.startChat("Bearer $token", StartChatRequest(username))
                        showNewChatDialog = false
                        onOpenChat(res.chatId, res.participant.display_name)
                    } catch (e: Exception) {
                        loadError = "Couldn't start chat — check the username and that the server is reachable."
                        showNewChatDialog = false
                    }
                }
            }
        )
    }
}

@Composable
private fun NewChatDialog(onDismiss: () -> Unit, onStart: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start a chat") },
        text = {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Their username") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (username.isNotBlank()) onStart(username.trim()) }) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ChatRow(thread: ChatThreadDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(LumiaSurfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            Text((thread.participant?.display_name?.firstOrNull() ?: '?').uppercase())
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                thread.participant?.display_name ?: thread.participant?.username ?: "Unknown",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                thread.lastMessage.ifBlank { "Say hello \uD83D\uDC4B" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
    Divider(color = LumiaSurfaceAlt, thickness = 0.5.dp, modifier = Modifier.padding(start = 80.dp))
}

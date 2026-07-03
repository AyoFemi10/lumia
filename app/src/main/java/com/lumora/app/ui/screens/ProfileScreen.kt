package com.lumora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lumora.app.repo.AuthRepository
import com.lumora.app.repo.SessionStore
import com.lumora.app.repo.SocketManager
import com.lumora.app.ui.theme.LumiaSurfaceAlt

@Composable
fun ProfileScreen(onLoggedOut: () -> Unit) {
    val context = LocalContext.current
    val sessionStore = remember { SessionStore(context) }
    val authRepo = remember { AuthRepository(sessionStore) }
    val session = remember { sessionStore.load() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.size(96.dp).clip(CircleShape).background(LumiaSurfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            Text(
                (session?.displayName?.firstOrNull() ?: '?').uppercase(),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(session?.displayName ?: "Unknown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("@${session?.username ?: "unknown"}", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = {
            SocketManager.disconnect()
            authRepo.logout()
            onLoggedOut()
        }) {
            Text("Log out")
        }
    }
}

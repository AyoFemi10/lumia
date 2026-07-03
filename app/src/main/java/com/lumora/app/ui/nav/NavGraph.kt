package com.lumora.app.ui.nav

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lumora.app.repo.SessionStore
import com.lumora.app.ui.screens.ChatDetailScreen
import com.lumora.app.ui.screens.ChatsScreen
import com.lumora.app.ui.screens.FeedScreen
import com.lumora.app.ui.screens.LoginScreen
import com.lumora.app.ui.screens.ProfileScreen
import com.lumora.app.ui.screens.SignupScreen

private sealed class Dest(val route: String, val label: String) {
    data object Feed : Dest("feed", "Feed")
    data object Chats : Dest("chats", "Chats")
    data object Profile : Dest("profile", "Profile")
}

private val bottomDestinations = listOf(Dest.Feed, Dest.Chats, Dest.Profile)

@Composable
fun LumiaNavGraph() {
    val context = LocalContext.current
    var loggedIn by remember { mutableStateOf(SessionStore(context).load() != null) }

    if (!loggedIn) {
        AuthNavHost(onAuthed = { loggedIn = true })
    } else {
        MainAppScaffold(onLoggedOut = { loggedIn = false })
    }
}

@Composable
private fun AuthNavHost(onAuthed: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoggedIn = onAuthed,
                onGoToSignup = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignupScreen(
                onSignedUp = onAuthed,
                onGoToLogin = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainAppScaffold(onLoggedOut: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            val hideBar = currentRoute?.route?.startsWith("chat_detail") == true

            if (!hideBar) {
                NavigationBar {
                    bottomDestinations.forEach { dest ->
                        val selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    when (dest) {
                                        Dest.Feed -> Icons.Filled.Home
                                        Dest.Chats -> Icons.Filled.ChatBubble
                                        Dest.Profile -> Icons.Filled.Person
                                    },
                                    contentDescription = dest.label
                                )
                            },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Feed.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Feed.route) { FeedScreen() }
            composable(Dest.Chats.route) {
                ChatsScreen(onOpenChat = { chatId, title ->
                    val encodedTitle = Uri.encode(title)
                    navController.navigate("chat_detail/$chatId/$encodedTitle")
                })
            }
            composable(Dest.Profile.route) {
                ProfileScreen(onLoggedOut = onLoggedOut)
            }
            composable("chat_detail/{chatId}/{title}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                val title = backStackEntry.arguments?.getString("title") ?: "Chat"
                ChatDetailScreen(chatId = chatId, title = title, onBack = { navController.popBackStack() })
            }
        }
    }
}

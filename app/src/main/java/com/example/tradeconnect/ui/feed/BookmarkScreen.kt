package com.example.tradeconnect.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.ui.feed.components.SidebarMenu
import com.example.tradeconnect.viewmodel.TweetViewModel
import kotlinx.coroutines.launch

@Composable
fun BookmarkScreen(
    navController: NavHostController,
    viewModel: TweetViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val currentUserId = viewModel.authVM.getCurrentUserId() ?: ""

    // Charger les tweets sauvegardés
    LaunchedEffect(Unit) {
        viewModel.loadSavedTweets()
    }

    val bookmarkedTweets = viewModel.savedTweets.value

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val bgColor = if (isDarkMode) Color.Black else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarMenu(
                navController = navController,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                onLogoutClick = {}
            )
        }
    ) {
        Scaffold(
            containerColor = bgColor,
            bottomBar = {
                BottomNavBar(
                    navController = navController,
                    currentRoute = "feed" // ou autre
                )

            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(bgColor)
            ) {

                // HEADER — Style Threads
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "menu", tint = textColor)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Bookmarks",
                        color = textColor,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // LISTE DES TWEETS SAUVEGARDÉS
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    items(bookmarkedTweets) { tweet ->
                        TweetItem(
                            tweet = tweet,
                            isDarkMode = isDarkMode,
                            currentUserId = currentUserId,
                            onMoreClick = {},
                            onLike = { id -> viewModel.toggleLike(id) },
                            onSave = { id -> viewModel.toggleSave(id) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

package com.example.tradeconnect.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.data.model.ChatPreview
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.ChatListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel
) {
    val chats by viewModel.chatPreviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }

                    IconButton(onClick = { navController.navigate("user_search") }) {
                        Icon(Icons.Default.Search, contentDescription = "Search users")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
            )
        }
    ) { padding ->

        when {
            isLoading && chats.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading conversations...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            chats.isEmpty() && !isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No messages yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { navController.navigate("user_search") }) {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start a conversation")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(chats) { chat ->
                        ChatPreviewItem(
                            chat = chat,
                            onClick = {
                                navController.navigate(
                                    "chat/${chat.user.uid}/${chat.user.username}"
                                )
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ChatPreviewItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            if (chat.user.profileImageUrl.isNotEmpty()) {
                if (chat.user.profileImageUrl.startsWith("data:image")) {
                    Base64ProfileImage(
                        base64String = chat.user.profileImageUrl,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                } else {
                    AsyncImage(
                        model = chat.user.profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                DefaultAvatar(
                    letter = chat.user.username.firstOrNull()?.uppercase() ?: "?",
                    modifier = Modifier.size(56.dp)
                )
            }

            if (chat.unreadCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Medium
                )

                if (chat.lastMessageTime > 0) {
                    Text(
                        text = formatChatTime(chat.lastMessageTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = chat.lastMessage.ifEmpty { "No messages yet" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (chat.unreadCount > 0)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatChatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        diff < 172_800_000 -> "Yesterday"
        diff < 604_800_000 ->
            SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        else ->
            SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}

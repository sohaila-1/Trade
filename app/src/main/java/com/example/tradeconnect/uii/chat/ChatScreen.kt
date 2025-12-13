package com.example.tradeconnect.ui.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.data.model.Message
import com.example.tradeconnect.data.model.MessageStatus
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.util.NetworkObserver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(

    navController: NavController,
    partnerId: String,
    messageRepository: MessageRepository,
    networkObserver: NetworkObserver,
    username: String
) {
    val scope = rememberCoroutineScope()

    // State managed directly in composable
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var partner by remember { mutableStateOf<User?>(null) }
    var messageText by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    Log.d("ChatScreen", "Recomposing with ${messages.size} messages")

    // Observe network status
    LaunchedEffect(Unit) {
        try {
            networkObserver.isConnected.collect { connected ->
                val wasOffline = !isOnline
                isOnline = connected
                Log.d("ChatScreen", "Network status: $connected")

                if (connected && wasOffline) {
                    Log.d("ChatScreen", "Coming online, syncing pending messages")
                    try {
                        messageRepository.syncPendingMessages()
                    } catch (e: CancellationException) {
                        throw e // Re-throw cancellation
                    } catch (e: Exception) {
                        Log.e("ChatScreen", "Error syncing pending messages", e)
                    }
                }
            }
        } catch (e: CancellationException) {
            // Normal cancellation when leaving the screen - don't log as error
            Log.d("ChatScreen", "Network observer cancelled")
        }
    }

    // Load messages
    LaunchedEffect(partnerId) {
        Log.d("ChatScreen", "Loading messages for partner: $partnerId")

        try {
            messageRepository.getMessagesForChat(partnerId)
                .catch { e ->
                    // Don't catch CancellationException in flow catch
                    if (e is CancellationException) throw e
                    Log.e("ChatScreen", "Error loading messages", e)
                    isLoading = false
                }
                .collect { msgs ->
                    messages = msgs
                    isLoading = false
                    Log.d("ChatScreen", "Messages updated: ${msgs.size} total")
                }
        } catch (e: CancellationException) {
            // Normal cancellation when leaving the screen - don't log as error
            Log.d("ChatScreen", "Message flow cancelled")
        } catch (e: Exception) {
            Log.e("ChatScreen", "Exception in message flow", e)
            isLoading = false
        }
    }

    // Load partner info
    LaunchedEffect(partnerId) {
        try {
            val user = messageRepository.getUserById(partnerId)
            partner = user
            Log.d("ChatScreen", "Loaded partner: ${user?.username}")
        } catch (e: CancellationException) {
            Log.d("ChatScreen", "Partner loading cancelled")
        } catch (e: Exception) {
            Log.e("ChatScreen", "Error loading partner", e)
        }
    }

    // Sync history if online
    LaunchedEffect(partnerId, isOnline) {
        if (isOnline) {
            try {
                Log.d("ChatScreen", "Syncing conversation history")
                messageRepository.syncConversationHistory(partnerId, limit = 50)
            } catch (e: CancellationException) {
                Log.d("ChatScreen", "History sync cancelled")
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error syncing history", e)
            }
        }
    }

    // Mark messages as seen (only once, not on recomposition)
    LaunchedEffect(partnerId) {
        try {
            messageRepository.markMessagesAsSeen(partnerId)
        } catch (e: CancellationException) {
            Log.d("ChatScreen", "Mark as seen cancelled")
        } catch (e: Exception) {
            Log.e("ChatScreen", "Error marking as seen (ignored)", e)
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messages.size - 1)
            } catch (e: CancellationException) {
                // Scroll was cancelled, that's fine
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Profile picture
                        if (partner != null) {
                            if (partner!!.profileImageUrl.isNotEmpty()) {
                                if (partner!!.profileImageUrl.startsWith("data:image")) {
                                    Base64ProfileImage(
                                        base64String = partner!!.profileImageUrl,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    AsyncImage(
                                        model = partner!!.profileImageUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                DefaultAvatar(
                                    letter = partner!!.username.firstOrNull()?.uppercase() ?: "?",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Column {
                            Text(
                                text = partner?.username ?: "Loading...",
                                style = MaterialTheme.typography.titleMedium
                            )
                            // Show online status or connection status
//                            Text(
//                                text = if (isOnline) "Online" else "Offline",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = if (isOnline)
//                                    MaterialTheme.colorScheme.primary
//                                else
//                                    MaterialTheme.colorScheme.onSurfaceVariant
//                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                MessageInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSendClick = { /* send */ },
                    enabled = messageText.isNotBlank()
                )

                Divider()

                BottomNavBar(
                    navController = navController,
                )
            }
        }

    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading && messages.isEmpty()) {
                // Loading state
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading messages...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (messages.isEmpty() && !isLoading) {
                // Empty state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start the conversation!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isSentByCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (message.isSentByCurrentUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isSentByCurrentUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isSentByCurrentUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )

                    // Show status for sent messages
                    if (message.isSentByCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(status = message.status)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageStatusIcon(status: MessageStatus) {
    val (icon, color) = when (status) {
        MessageStatus.PENDING -> "⏱" to Color.Gray
        MessageStatus.SENT -> "✓" to Color.White.copy(alpha = 0.7f)
        MessageStatus.DELIVERED -> "✓✓" to Color.White.copy(alpha = 0.7f)
        MessageStatus.SEEN -> "✓✓" to Color.Blue
    }

    Text(
        text = icon,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("Type a message...") },
                maxLines = 5,
                shape = RoundedCornerShape(24.dp)
            )

            IconButton(
                onClick = onSendClick,
                enabled = enabled,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        diff < 172800_000 -> "Yesterday ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))}"
        else -> SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
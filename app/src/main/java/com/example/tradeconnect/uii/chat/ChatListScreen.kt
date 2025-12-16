package com.example.tradeconnect.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.data.model.ChatPreview
import com.example.tradeconnect.ui.feed.components.BottomNavBar
import com.example.tradeconnect.ui.theme.*
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.ChatListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel,
    isDarkMode: Boolean
) {
    val chats by viewModel.chatPreviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    // Couleurs
    val bgColor = if (isDarkMode) DarkBackground else LightBackground
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val dividerColor = if (isDarkMode) DarkDivider else LightDivider
    val cardColor = if (isDarkMode) DarkSurface else LightSurface

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = bgColor,
                shadowElevation = 2.dp
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Bouton retour
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = textColor
                            )
                        }

                        // Titre
                        Text(
                            text = "Messages",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        // Actions
                        Row {
                            // Refresh button
                            IconButton(
                                onClick = { viewModel.refresh() },
                                enabled = !isSyncing
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp,
                                        color = TwitterBlue
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Refresh,
                                        "Actualiser",
                                        tint = textColor
                                    )
                                }
                            }

                            // Search button
                            IconButton(onClick = { navController.navigate("user_search") }) {
                                Icon(
                                    Icons.Outlined.Search,
                                    "Rechercher",
                                    tint = textColor
                                )
                            }
                        }
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                }
            }
        },
        bottomBar = {
            BottomNavBar(navController = navController, isDarkMode = isDarkMode)
        }
    ) { padding ->
        when {
            // Loading
            isLoading && chats.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = TwitterBlue)
                        Text(
                            text = "Chargement des conversations...",
                            color = secondaryColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Empty state
            chats.isEmpty() && !isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Icône
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            tint = TwitterBlue,
                            modifier = Modifier.size(80.dp)
                        )

                        Text(
                            text = "Aucun message",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Text(
                            text = "Commencez une conversation avec quelqu'un",
                            fontSize = 14.sp,
                            color = secondaryColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { navController.navigate("user_search") },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TwitterBlue,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Nouvelle conversation",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Chat list
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(bgColor)
                ) {
                    items(chats) { chat ->
                        ChatPreviewItem(
                            chat = chat,
                            isDarkMode = isDarkMode,
                            onClick = {
                                navController.navigate("chat/${chat.user.uid}/${chat.user.username}")
                            }
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = dividerColor,
                            modifier = Modifier.padding(start = 84.dp)
                        )
                    }

                    // Espace en bas
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatPreviewItem(
    chat: ChatPreview,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDarkMode) DarkText else LightText
    val secondaryColor = if (isDarkMode) DarkGrayText else LightGrayText
    val bgColor = if (isDarkMode) DarkBackground else LightBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile image with unread badge
        Box {
            when {
                chat.user.profileImageUrl.startsWith("data:image") ||
                        chat.user.profileImageUrl.length > 200 -> {
                    Base64ProfileImage(
                        base64String = chat.user.profileImageUrl,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                }
                chat.user.profileImageUrl.isNotEmpty() &&
                        (chat.user.profileImageUrl.startsWith("http") ||
                                chat.user.profileImageUrl.startsWith("https")) -> {
                    AsyncImage(
                        model = chat.user.profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    DefaultAvatar(
                        letter = chat.user.username.firstOrNull()?.uppercase() ?: "?",
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Online indicator
            if (chat.user.isOnline) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(OnlineGreen)
                        .align(Alignment.BottomEnd)
                )
            }

            // Unread badge
            if (chat.unreadCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    shape = CircleShape,
                    color = TwitterBlue
                ) {
                    Text(
                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Chat info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.user.username,
                    fontSize = 16.sp,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )

                if (chat.lastMessageTime > 0) {
                    Text(
                        text = formatChatTime(chat.lastMessageTime),
                        fontSize = 13.sp,
                        color = if (chat.unreadCount > 0) TwitterBlue else secondaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = chat.lastMessage.ifEmpty { "Aucun message" },
                fontSize = 14.sp,
                color = if (chat.unreadCount > 0) textColor else secondaryColor,
                fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
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
        diff < 60_000 -> "À l'instant"
        diff < 3600_000 -> "${diff / 60_000} min"
        diff < 86400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        diff < 172800_000 -> "Hier"
        diff < 604800_000 -> SimpleDateFormat("EEE", Locale.FRANCE).format(Date(timestamp))
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
    }
}
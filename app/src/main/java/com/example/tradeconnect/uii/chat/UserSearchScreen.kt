package com.example.tradeconnect.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.UserSearchViewModel
import com.example.tradeconnect.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(
    navController: NavController,
    viewModel: UserSearchViewModel,
    userViewModel: UserViewModel? = null // Optionnel pour le follow
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val followingStatus by userViewModel?.followingStatus?.collectAsState() ?: remember { mutableStateOf(emptyMap()) }
    val followLoadingUserId = userViewModel?.isFollowLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search users...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Search")
                },
                singleLine = true
            )

            // Loading indicator
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Search results
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults) { user ->
                    UserItemWithFollow(
                        user = user,
                        isFollowing = followingStatus[user.uid] ?: false,
                        isLoading = followLoadingUserId == user.uid,
                        onUserClick = {
                            navController.navigate("chat/${user.uid}/${user.username}")
                        },
                        onProfileClick = {
                            navController.navigate("other_profile/${user.uid}")
                        },
                        onFollowClick = {
                            userViewModel?.toggleFollow(user.uid)
                        },
                        showFollowButton = userViewModel != null
                    )
                }

                // Empty state
                if (!viewModel.isLoading && searchResults.isEmpty() && viewModel.searchQuery.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItemWithFollow(
    user: User,
    isFollowing: Boolean,
    isLoading: Boolean,
    onUserClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFollowClick: () -> Unit,
    showFollowButton: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile image - clickable pour voir le profil
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onProfileClick)
        ) {
            if (user.profileImageUrl.isNotEmpty()) {
                if (user.profileImageUrl.startsWith("data:image")) {
                    Base64ProfileImage(
                        base64String = user.profileImageUrl,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                DefaultAvatar(
                    letter = user.username.firstOrNull()?.uppercase() ?: "?",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // User info - clickable pour voir le profil
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onProfileClick)
        ) {
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Afficher le statut en ligne
            if (user.isOnline) {
                Text(
                    text = "En ligne",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }

        // Bouton Follow
        if (showFollowButton) {
            Button(
                onClick = onFollowClick,
                enabled = !isLoading,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.LightGray else TBlue,
                    contentColor = if (isFollowing) Color.Black else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = if (isFollowing) Color.Black else Color.White
                    )
                } else {
                    Text(
                        text = if (isFollowing) "Suivi" else "Suivre",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Garder l'ancien UserItem pour compatibilité
@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    UserItemWithFollow(
        user = user,
        isFollowing = false,
        isLoading = false,
        onUserClick = onClick,
        onProfileClick = onClick,
        onFollowClick = {},
        showFollowButton = false
    )
}

package com.example.tradeconnect.ui.follow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.tradeconnect.viewmodel.FollowViewModel

enum class FollowListType {
    FOLLOWERS, FOLLOWING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    navController: NavController,
    followViewModel: FollowViewModel,
    userId: String,
    listType: FollowListType,
    isDarkMode: Boolean
) {
    val followers by followViewModel.followers.collectAsState()
    val following by followViewModel.following.collectAsState()
    val followingStatus by followViewModel.followingStatus.collectAsState()
    val isLoading = followViewModel.isLoading
    val followLoadingUserId = followViewModel.isFollowLoading

    val users = when (listType) {
        FollowListType.FOLLOWERS -> followers
        FollowListType.FOLLOWING -> following
    }

    val title = when (listType) {
        FollowListType.FOLLOWERS -> "Followers"
        FollowListType.FOLLOWING -> "Abonnements"
    }

    // Charger les données
    LaunchedEffect(userId, listType) {
        when (listType) {
            FollowListType.FOLLOWERS -> followViewModel.loadFollowers(userId)
            FollowListType.FOLLOWING -> followViewModel.loadFollowing(userId)
        }
    }

    // Nettoyer à la sortie
    DisposableEffect(Unit) {
        onDispose {
            followViewModel.clearFollowLists()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TBlue)
                }
            }
            users.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (listType) {
                                FollowListType.FOLLOWERS -> "😕"
                                FollowListType.FOLLOWING -> "🔍"
                            },
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (listType) {
                                FollowListType.FOLLOWERS -> "Aucun follower"
                                FollowListType.FOLLOWING -> "Aucun abonnement"
                            },
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        FollowUserItem(
                            user = user,
                            isFollowing = followingStatus[user.uid] ?: false,
                            isLoading = followLoadingUserId == user.uid,
                            isCurrentUser = followViewModel.isCurrentUser(user.uid),
                            onUserClick = {
                                navController.navigate("user_profile/${user.uid}")
                            },
                            onFollowClick = {
                                followViewModel.toggleFollow(user.uid)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FollowUserItem(
    user: User,
    isFollowing: Boolean,
    isLoading: Boolean,
    isCurrentUser: Boolean,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo de profil
            if (user.profileImageUrl.isNotEmpty()) {
                if (user.profileImageUrl.startsWith("data:image")) {
                    Base64ProfileImage(
                        base64String = user.profileImageUrl,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                } else {
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                DefaultAvatar(
                    letter = user.username.firstOrNull()?.uppercase() ?: "?",
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Infos utilisateur
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = user.email,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                // Statut en ligne
                if (user.isOnline) {
                    Text(
                        text = "🟢 En ligne",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Bouton Follow (pas pour soi-même)
            if (!isCurrentUser) {
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
}
package com.example.tradeconnect.ui.follow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tradeconnect.ui.theme.TBlue
import com.example.tradeconnect.util.Base64ProfileImage
import com.example.tradeconnect.util.DefaultAvatar
import com.example.tradeconnect.viewmodel.FollowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPublicProfileScreen(
    navController: NavController,
    followViewModel: FollowViewModel,
    userId: String,
    isDarkMode: Boolean
) {
    val selectedUser by followViewModel.selectedUser.collectAsState()
    val currentUser by followViewModel.currentUser.collectAsState()
    val followingStatus by followViewModel.followingStatus.collectAsState()

    val isFollowing = followingStatus[userId] ?: false
    val isOwnProfile = followViewModel.isCurrentUser(userId)
    val isLoading = followViewModel.isLoading
    val isFollowLoading = followViewModel.isFollowLoading == userId

    // Charger le profil
    LaunchedEffect(userId) {
        followViewModel.loadUserProfile(userId)
    }

    // Nettoyer à la sortie
    DisposableEffect(Unit) {
        onDispose {
            followViewModel.clearSelectedUser()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedUser?.username ?: "Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && selectedUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TBlue)
            }
        } else {
            selectedUser?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Photo de profil
                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profileImageUrl.isNotEmpty()) {
                            if (user.profileImageUrl.startsWith("data:image")) {
                                Base64ProfileImage(
                                    base64String = user.profileImageUrl,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                AsyncImage(
                                    model = user.profileImageUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            DefaultAvatar(
                                letter = user.username.firstOrNull()?.uppercase() ?: "?",
                                modifier = Modifier.size(120.dp)
                            )
                        }

                        // Indicateur en ligne
                        if (user.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nom d'utilisateur
                    Text(
                        text = user.username,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Email
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    // Statut en ligne
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (user.isOnline) "🟢 En ligne" else "⚫ ${user.getLastSeenText()}",
                        fontSize = 12.sp,
                        color = if (user.isOnline) Color(0xFF4CAF50) else Color.Gray
                    )

                    // Badge "Vous suit"
                    if (!isOwnProfile && currentUser?.followers?.contains(userId) == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Vous suit",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = TBlue
                            )
                        }
                    }

                    // Bio
                    if (user.bio.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = user.bio,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats followers/following
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Followers
                            Column(
                                modifier = Modifier.clickable {
                                    navController.navigate("followers/$userId")
                                },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = user.followersCount.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Followers",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            // Séparateur
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            )

                            // Following
                            Column(
                                modifier = Modifier.clickable {
                                    navController.navigate("following/$userId")
                                },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = user.followingCount.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Abonnements",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Boutons d'action
                    if (!isOwnProfile) {
                        // Bouton Follow/Unfollow
                        Button(
                            onClick = { followViewModel.toggleFollow(userId) },
                            enabled = !isFollowLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Color.LightGray else TBlue,
                                contentColor = if (isFollowing) Color.Black else Color.White
                            )
                        ) {
                            if (isFollowLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = if (isFollowing) Color.Black else Color.White
                                )
                            } else {
                                Text(
                                    text = if (isFollowing) "Ne plus suivre" else "Suivre",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bouton Message
                        OutlinedButton(
                            onClick = {
                                navController.navigate("chat/${user.uid}/${user.username}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Envoyer un message", fontSize = 16.sp)
                        }
                    } else {
                        // C'est son propre profil
                        OutlinedButton(
                            onClick = { navController.navigate("profile") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text("Modifier le profil", fontSize = 16.sp)
                        }
                    }

                    // Erreur
                    followViewModel.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Utilisateur non trouvé", color = Color.Gray)
                }
            }
        }
    }
}